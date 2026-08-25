package com.jakt.aiplatform.common.framework.result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
/**
 * common-util 层通用分页结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private int pageNum = 1;
    private int pageSize = 10;
    private List<T> dataList;
}
