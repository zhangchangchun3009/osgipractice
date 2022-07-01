package osgi.common.vo;

/**
 * 分页对象
 * 
 * @author zhangchangchun
 * @since 2021年4月1日
 */
public class PageVO {
    /**
     * 第几页
     */
    private int pageNum = 1;

    /**
     * 每页数量，不大于1000
     */
    private int pageSize = 15;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        if (pageNum <= 0) {
            this.pageNum = 1;
            return;
        }
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize > 1000 || pageSize <= 0) {
            this.pageSize = 15;
            return;
        }
        this.pageSize = pageSize;
    }

}
