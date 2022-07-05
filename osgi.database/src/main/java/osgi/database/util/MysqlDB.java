package osgi.database.util;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class MysqlDB {

    private static final DataSource dbcp2DS;

    private static final Configuration configuration;

    private static final List<Class<?>> mybatisMappers = new ArrayList<>(32);

    private static final Map<String, Object> mapperProxys = new HashMap<>(32);

    private static final ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            InputStream in = MysqlDB.class.getClassLoader().getResourceAsStream("dbcp2.properties");
            Properties prop = new Properties();
            prop.load(in);
            dbcp2DS = BasicDataSourceFactory.createDataSource(prop);
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("all", transactionFactory, dbcp2DS);
            configuration = new Configuration(environment);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void registMappers(Collection<Class<?>> mappers) {
        mybatisMappers.addAll(mappers);
        rebuildSqlSessionFactory(mappers);
        for (Class<?> clazz : mappers) {
            Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            SqlSession sqlSession = getOrCreateSqlSession();
                            Object boundMapper = sqlSession.getMapper(clazz);
                            try {
                                Object result = method.invoke(boundMapper, args);
                                sqlSession.commit(true);
                                return result;
                            } catch (Throwable t) {
                                sqlSession.rollback(true);
                                throw t;
                            } finally {
                                if (sqlSession != null) {
                                    closeSqlSession(sqlSession);
                                }
                            }
                        }
                    });
            mapperProxys.put(clazz.getName(), proxy);
        }
    }

    private static void rebuildSqlSessionFactory(Collection<Class<?>> mybatisMappers) {
        for (Class<?> clazz : mybatisMappers) {
            configuration.addMapper(clazz);
        }
        sqlSessionFactory = null;
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMapper(Class<T> mapper) {
        Object mapperProxy = mapperProxys.get(mapper.getName());
        if (mapperProxy != null && mapper.isInstance(mapperProxy)) {
            return (T) mapperProxy;
        }
        throw new NoSuchElementException("mapper for " + mapper.getName() + "has not registered");
    }

    private static SqlSession getOrCreateSqlSession() {
        SqlSession sqlSession = sqlSessionThreadLocal.get();
        if (sqlSession != null) {
            return sqlSession;
        }
        sqlSession = sqlSessionFactory.openSession();
        sqlSessionThreadLocal.set(sqlSession);
        return sqlSession;
    }

    private static void closeSqlSession(SqlSession sqlSession) {
        sqlSession.close();
        sqlSessionThreadLocal.remove();
    }
}
