
package osgi.database.dao;

import org.apache.ibatis.annotations.Param;

/**
 * The Interface ISequenceDao.
 */
public interface ISequenceDao {

    /**
     * Gets the next value.
     *
     * @param sequenceName the sequence name
     * @return the next value
     */
    long getNextValue(@Param("name") String sequenceName);
}
