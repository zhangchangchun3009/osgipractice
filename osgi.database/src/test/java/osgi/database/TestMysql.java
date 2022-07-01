package osgi.database;

import java.util.ArrayList;
import java.util.List;

import osgi.database.dao.ISequenceDao;
import osgi.database.util.MysqlDB;

public class TestMysql {
    public static void main(String[] args) {
        List<Class<?>> mappers = new ArrayList<>();
        mappers.add(ISequenceDao.class);
        MysqlDB.registMappers(mappers);
        ISequenceDao dao = MysqlDB.getMapper(ISequenceDao.class);
        System.out.println(dao.getNextValue("s_common"));
    }
}
