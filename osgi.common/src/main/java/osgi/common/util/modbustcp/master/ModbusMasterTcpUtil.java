
package osgi.common.util.modbustcp.master;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.digitalpetri.modbus.codec.Modbus;
import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.requests.ReadCoilsRequest;
import com.digitalpetri.modbus.requests.ReadDiscreteInputsRequest;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.ReadInputRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.responses.ModbusResponse;
import com.digitalpetri.modbus.responses.ReadCoilsResponse;
import com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.ReadInputRegistersResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

/**
 * The Class ModbusMasterTcpUtil.
 *
 * @author zcc
 * @since 2021年12月13日
 */
public class ModbusMasterTcpUtil {

    /**
     * Gets the master instance.
     *
     * @param ip the ip
     * @param port the port
     * @return the master instance
     */
    public static ModbusTcpMaster getMasterInstance(String ip, int port) {
        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(ip).setPort(port).build();
        ModbusTcpMaster master = new ModbusTcpMaster(config);
        master.connect();
        return master;
    }

    /***
     * 释放资源
     */
    public static void release(ModbusTcpMaster master) {
        if (master != null) {
            master.disconnect();
        }
        Modbus.releaseSharedResources();
    }

    /**
     * 读取Coils开关量
     *
     * @param address 寄存器开始地址
     * @param quantity 数量
     * @param unitId ID
     * @return 读取值
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public static synchronized Boolean readCoils(ModbusTcpMaster master, int address, int quantity, int unitId)
            throws InterruptedException, ExecutionException {
        Boolean result = null;
        CompletableFuture<ReadCoilsResponse> future = master.sendRequest(new ReadCoilsRequest(address, quantity),
                unitId);
        ReadCoilsResponse readCoilsResponse = future.get();// 工具类做的同步返回.实际使用推荐结合业务进行异步处理
        if (readCoilsResponse != null) {
            ByteBuf buf = readCoilsResponse.getCoilStatus();
            result = buf.readBoolean();
            ReferenceCountUtil.release(readCoilsResponse);
        }
        return result;
    }

    /**
     * 读取readDiscreteInputs开关量
     *
     * @param address 寄存器开始地址
     * @param quantity 数量
     * @param unitId ID
     * @return 读取值
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public static synchronized Boolean readDiscreteInputs(ModbusTcpMaster master, int address, int quantity, int unitId)
            throws InterruptedException, ExecutionException {
        Boolean result = null;
        CompletableFuture<ReadDiscreteInputsResponse> future = master
                .sendRequest(new ReadDiscreteInputsRequest(address, quantity), unitId);
        ReadDiscreteInputsResponse discreteInputsResponse = future.get();// 工具类做的同步返回.实际使用推荐结合业务进行异步处理
        if (discreteInputsResponse != null) {
            ByteBuf buf = discreteInputsResponse.getInputStatus();
            result = buf.readBoolean();
            ReferenceCountUtil.release(discreteInputsResponse);
        }
        return result;
    }

    /**
     * 读取HoldingRegister数据
     *
     * @param address 寄存器地址
     * @param quantity 寄存器数量
     * @param unitId id
     * @return 读取结果
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public static synchronized byte[] readHoldingRegisters(ModbusTcpMaster master, int address, int quantity,
            int unitId) throws InterruptedException, ExecutionException {
        byte[] result = null;
        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(address, quantity);
        CompletableFuture<ReadHoldingRegistersResponse> future = master.sendRequest(request, unitId);
        ReadHoldingRegistersResponse readHoldingRegistersResponse = future.get();// 工具类做的同步返回.实际使用推荐结合业务进行异步处理
        if (readHoldingRegistersResponse != null) {
            ByteBuf buf = readHoldingRegistersResponse.getRegisters();
            result = new byte[buf.readableBytes()];
            for (int i = 0; i < result.length; i++) {
                result[i] = buf.getByte(i);
            }
            ReferenceCountUtil.release(readHoldingRegistersResponse);
        }
        return result;
    }

    /**
     * 读取单个HoldingRegister数据
     *
     * @param address 寄存器地址
     * @param unitId id
     * @return 读取结果
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public static synchronized int readSingleHoldingRegisters(ModbusTcpMaster master, int address, int unitId)
            throws InterruptedException, ExecutionException {
        int result = 0;
        ReadHoldingRegistersRequest request = new ReadHoldingRegistersRequest(address, 1);
        CompletableFuture<ReadHoldingRegistersResponse> future = master.sendRequest(request, unitId);
        ReadHoldingRegistersResponse readHoldingRegistersResponse = future.get();// 工具类做的同步返回.实际使用推荐结合业务进行异步处理
        if (readHoldingRegistersResponse != null) {
            ByteBuf buf = readHoldingRegistersResponse.getRegisters();
            result = buf.readUnsignedShort();
            ReferenceCountUtil.release(readHoldingRegistersResponse);
        }
        return result;
    }

    /**
     * 读取InputRegisters模拟量数据
     *
     * @param address 寄存器开始地址
     * @param quantity 数量
     * @param unitId ID
     * @return 读取值
     * @throws InterruptedException 异常
     * @throws ExecutionException 异常
     */
    public static synchronized ByteBuf readInputRegisters(ModbusTcpMaster master, int address, int quantity, int unitId)
            throws InterruptedException, ExecutionException {
        CompletableFuture<ReadInputRegistersResponse> future = master
                .sendRequest(new ReadInputRegistersRequest(address, quantity), unitId);
        ReadInputRegistersResponse readInputRegistersResponse = future.get();// 工具类做的同步返回.实际使用推荐结合业务进行异步处理
        if (readInputRegistersResponse != null) {
            try {
                return readInputRegistersResponse.getRegisters();
            } finally {
                ReferenceCountUtil.release(readInputRegistersResponse);
            }
        }
        return null;
    }

    // 功能码0X06 写入单个寄存器
    public static synchronized void WriteSingleRegisterRequest(ModbusTcpMaster master, int address, int value,
            int unitId) {// 发送单个寄存器数据，一般是无符号16位值：比如10
        CompletableFuture<ModbusResponse> res = master.sendRequest(new WriteSingleRegisterRequest(address, value),
                unitId);
        //经测试PLC写寄存器不能连续调用，必须等待信号传输，否则会卡住连接导致写不进去.读的效率高没这个问题
        try {
            res.get();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }

    // 功能码0X10 写入多个寄存器
    public static synchronized void WriteMultipleRegistersRequest(ModbusTcpMaster master, int address, byte[] values,
            int quantity, int unitId) {
        // 转netty需要的字节类型
        ByteBuf byteBuf = Unpooled.wrappedBuffer(values);
        // 发送多个寄存器数据，数据类型由quantity决定，2是float类型，4是double类型
        master.sendRequest(new WriteMultipleRegistersRequest(address, quantity, byteBuf), unitId);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        // 初始化资源
        ModbusTcpMaster master = ModbusMasterTcpUtil.getMasterInstance("127.0.0.1", 502);
        try {
            // 读取开关量
            // System.out.println(readCoils(0, 1, 1));
            // System.out.println(readDiscreteInputs(0, 1, 1));
            // System.out.println(readDiscreteInputs(1, 1, 1));
            for (int i = 0; i < 10; i++) {
                long t1 = System.currentTimeMillis();
                ModbusMasterTcpUtil.WriteSingleRegisterRequest(master, i, i, 1);
                System.out.println("" + (System.currentTimeMillis() - t1));
            }
            for (int i = 0; i < 10; i++) {
                int r = ModbusMasterTcpUtil.readSingleHoldingRegisters(master, i, 1);
                // System.out.println(r);
                // 读取模拟量
                System.out.println("1 " + i + ":" + Integer.toBinaryString(r));
            }
            byte[] res = ModbusMasterTcpUtil.readHoldingRegisters(master, 0, 4, 1);
            for (byte bt : res) {
                System.out.println(Integer.toBinaryString(bt));
            }
            // System.out.println(readHoldingRegisters(2, 2, 1));
            // System.out.println(readHoldingRegisters(4, 2, 1));
            // System.out.println(readInputRegisters(2, 4, 1));
            // System.out.println(readInputRegisters(6, 4, 1));
//            int value = Integer.parseInt("1000000010000001", 2);
//            System.out.println(value);
//            ModbusMasterTcpUtil.WriteSingleRegisterRequest(master, 0, value, 1);
//            Thread.sleep(2000);
//            int r2 = ModbusMasterTcpUtil.readSingleHoldingRegisters(master, 0, 1);
//            System.out.println(r2);
//            System.out.println(Integer.toBinaryString(r2));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源  不释放资源会保持连接，虚拟机无法关闭
            ModbusMasterTcpUtil.release(master);
        }
    }

}
