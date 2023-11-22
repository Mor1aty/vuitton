package com.moriaty.vuitton.bean.novel;

import lombok.Data;

/**
 * <p>
 * 获取目录 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 22:14
 */
@Data
public class FindCatalogueReq {

    private String downloaderMark;

    private String catalogueAppend;

}
