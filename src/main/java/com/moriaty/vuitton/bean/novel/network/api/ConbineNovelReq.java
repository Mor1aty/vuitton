package com.moriaty.vuitton.bean.novel.network.api;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * 下载小说 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/10/28 18:16
 */
@Data
public class ConbineNovelReq {

    private List<String> novelFileList;

    private String novelName;

}
