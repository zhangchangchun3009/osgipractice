package osgi.common.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 增改删批处理对象
 * 
 * @author zhangchangchun
 * @since 2021年4月1日
 * @param <T> 泛型类型
 */
public class BatchVO<T> implements Serializable {

    private static final long serialVersionUID = -3493778749985821370L;

    /**
     * 创建集合
     */
    private List<T> itemsToCreate;

    /**
     * 修改集合
     */
    private List<T> itemsToUpdate;

    /**
     * 删除集合
     */
    private List<T> itemsToDelete;

    public List<T> getItemsToCreate() {
        return itemsToCreate;
    }

    public void setItemsToCreate(List<T> itemsToCreate) {
        this.itemsToCreate = itemsToCreate;
    }

    public List<T> getItemsToUpdate() {
        return itemsToUpdate;
    }

    public void setItemsToUpdate(List<T> itemsToUpdate) {
        this.itemsToUpdate = itemsToUpdate;
    }

    public List<T> getItemsToDelete() {
        return itemsToDelete;
    }

    public void setItemsToDelete(List<T> itemsToDelete) {
        this.itemsToDelete = itemsToDelete;
    }

}
