package utils;

import java.util.HashSet;

/**
 * @Author: HanJiafeng
 * @Date: 15:50 2018/3/13
 * @Desc: 忽视大小写检索是否有字符串相同
 */
public class InSensitiveSet extends HashSet<String> {

    @Override
    public boolean contains(Object o) {

        if (o == null){
            return false;
        }

        //遍历Set中的所有元素,忽视大小写检索是否有字符串相同
        for (String e : this) {
            if (e.equalsIgnoreCase(o.toString())) {
                return true;
            }
        }

        return false;
    }
}
