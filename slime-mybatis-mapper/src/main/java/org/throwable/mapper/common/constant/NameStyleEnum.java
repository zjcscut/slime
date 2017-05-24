package org.throwable.mapper.common.constant;


import org.throwable.exception.UnsupportedElementException;

/**
 * @author throwable
 * @version v1.0
 * @description 命名转换枚举
 * @since 2017/3/30 13:06
 */
public enum NameStyleEnum {

    NORMAL,

    CAMELCASE_TO_UNDERLINE,

    CAMELCASE_TO_UNDERLINE_UPPERCASE,

    CAMELCASE_TO_UNDERLINE_LOWERCASE,

    UPPER,

    LOWER;

    public static NameStyleEnum search(String value) {
        NameStyleEnum[] enums = NameStyleEnum.values();
        for (NameStyleEnum styleEnum : enums) {
            if (styleEnum.name().equalsIgnoreCase(value)) {
                return styleEnum;
            }
        }
        throw new UnsupportedElementException(String.format("没有对应的命名转换类型:%s", value));
    }
}
