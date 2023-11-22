package com.moriaty.vuitton.bean.video;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 获取视频 Req
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/21 1:37
 */
@Data
@Accessors(chain = true)
public class FindVideoReq {

    private Integer id;

    private String name;

}
