package osgi.common.websocket.service;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.ConnectionClosedException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import osgi.common.util.AuthorizationUtil;
import osgi.common.util.JacksonUtil;
import osgi.common.vo.AuthorizationVO;
import osgi.common.vo.Response;
import osgi.common.vo.UserVO;
import osgi.common.websocket.service.impl.WebsocketServiceResolver;

public class ScmWebSocketServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScmWebSocketServer.class);

    private AtomicInteger connectionCount = new AtomicInteger(0);

    private ConcurrentHashMap<Integer, WebSocket> connections = new ConcurrentHashMap<>();

    private static WebsocketServiceResolver resolver = new WebsocketServiceResolver();

    private static ObjectMapper mapper = JacksonUtil.getDefaultObjectMapper();

    public ScmWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        LOGGER.info("websocket Server start at port:" + port);
    }

    public int getConnectionNum() {
        return connectionCount.get();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("WebSocket client closing...");
        int onlineNum = connectionCount.decrementAndGet();
        LOGGER.info("now link number is " + onlineNum);
        for (Entry<Integer, WebSocket> ele : connections.entrySet()) {
            if (conn == ele.getValue()) {
                connections.remove(ele.getKey());
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception e) {
        LOGGER.error("WebSocket handler occurs an exception,", e);
    }

    /**
     * 前端请求后端服务才会触发此方法
     * */
    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.debug("WebSocket server received a new message from client: " + message);
        try {
            Response<String> request = mapper.readValue(message, new TypeReference<Response<String>>() {
            });
            String requestType = request.getCode(); // 功能码
            String path = request.getMessage(); // 请求服务路径，两段式
            String param = request.getData(); // 请求参数，前后端约定，通常为json
            String response = resolver.handleMessage(requestType, path, param); // 服务返回数据，通常为json
            if (response != null && !response.equals("")) {
                conn.send(response);
            }
        } catch (Exception e) {
            LOGGER.error("handle websocket request err", e);
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        LOGGER.info("WebSocket open a new connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        int onlineNum = connectionCount.incrementAndGet();
        LOGGER.info("now link number is " + onlineNum);
        String descriptor = clientHandshake.getResourceDescriptor();
        LOGGER.debug("url: " + descriptor);
        String token = descriptor.substring(descriptor.lastIndexOf("/") + 1);
        try {
            AuthorizationVO authVO = AuthorizationUtil.decryptToken(token);
            UserVO user = null;
            try {
                user = authVO.getUser();
            } catch (Exception e) {
            }
            if (user == null) {
                return;
            }
            Integer userId = user.getUserId();
            connections.putIfAbsent(userId, conn);
        } catch (Exception e) {
            LOGGER.error("WebSocket getUser e,", e);
        }
    }

    @Override
    public void onStart() {
        LOGGER.info("WebSocket Server starting...");
    }

    /**
     * 服务端广播消息给所有客户端
     * */
    @Override
    public void broadcast(String text) {
        super.broadcast(text);
    }

    /**
     * 服务端发送消息给某个客户端，后续依使用需求可改造为支持单账号多开客户端
     * */
    public void send(Integer userId, String message) throws ConnectException, ConnectionClosedException {
        WebSocket connection = connections.get(userId);
        if (connection == null) {
            throw new ConnectException("can't find connection for this client,may client reopen and login again");
        }
        if (connection.isClosed() || connection.isClosing()) {
            connections.remove(userId);
            throw new ConnectionClosedException();
        }
        connection.send(message);
    }

}
