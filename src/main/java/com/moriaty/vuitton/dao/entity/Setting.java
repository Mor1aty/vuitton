package com.moriaty.vuitton.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 设置
 * </p>
 *
 * @author Moriaty
 * @since 2023/12/3 17:19
 */
@Data
@TableName("`setting`")
@Accessors(chain = true)
public class Setting {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("`key`")
    private String key;

    @TableField("`value`")
    private String value;
}
