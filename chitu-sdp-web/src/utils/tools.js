import store from '@/store'

const lookupArr = store.getters.dict

export const lookupOptions = (code) => {
    if (lookupArr) {
        return lookupArr[code] || []
    } else {
        console.error('加载字典过慢 字典' + code + '未加载成功')
        return []
    }
}

export const formatTime = (time) => {
    if (!time) return '0(ms)'
    if (time < 1000) {
        return time + ' (ms)'
    } else if (time < 60000) {
        return time / 1000 + ' (s)'
    } else if (time < 3600000) {
        return (Math.floor(time / 60000) + ' (min) ') + (Math.floor((time - Math.floor(time / 60000) * 60000) / 1000) + ' (s) ') + ((time - Math.floor(time / 1000) * 1000) + ' (ms)')
    } else {
        return (Math.floor(time / 3600000) + '(h) ') + (Math.floor((time - Math.floor(time / 3600000) * 3600000) / 60000) + ' (min) ') + (Math.floor((time - Math.floor(time / 60000) * 60000) / 1000) + ' (s) ') + ((time - Math.floor(time / 1000) * 1000) + ' (ms)')
    }
}
// console.log(formatTime(600001))

// 比对两字符串高亮标注函数
export const getHeightDiff = (str1 = "", str2 = "") => {
    class StringBuffer {
        constructor(str) {
            this.strings = []
            this.str = str
        }
        append (str) {
            return this.strings.push(str)
        }
        toString () {
            return this.strings.join('');
        }
    }

    const getHighLightDifferent = (a, b) => {
        let temp = getDiffArray(a, b);
        let a1 = getHighLight(a, temp[0]);
        let a2 = getHighLight(b, temp[1]);
        return new Array(a1, a2);
    }

    const getDiffArray = (a, b) => {
        let result = new Array();

        if (a.length < b.length) {
            let start = 0;
            let end = a.length;
            result = getDiff(a, b, start, end);
        } else {
            let start = 0;
            let end = b.length;
            result = getDiff(b, a, start, end);
            result = new Array(result[1], result[0]);
        }
        return result;
    }

    const getDiff = (a, b, start, end) => {
        let result = new Array(a, b);
        let len = result[0].length;
        while (len > 0) {
            for (let i = start; i < end - len + 1; i++) {
                let sub = result[0].substring(i, i + len);
                let idx = -1;
                if ((idx = result[1].indexOf(sub)) != -1) {
                    result[0] = setEmpty(result[0], i, i + len);
                    result[1] = setEmpty(result[1], idx, idx + len);
                    if (i > 0) {
                        //递归获取空白区域左边差异
                        result = getDiff(result[0], result[1], start, i);
                    }
                    if (i + len < end) {
                        //递归获取空白区域右边差异
                        result = getDiff(result[0], result[1], i + len, end);
                    }
                    len = 0;//退出while循环
                    break;
                }
            }
            len = parseInt(len / 2);
        }
        return result;
    }

    const setEmpty = (s, start, end) => {
        let array = s.split("");
        for (let i = start; i < end; i++) {
            array[i] = ' ';
        }
        return array.join("");
    }

    const getHighLight = (source, temp) => {
        let result = new StringBuffer();
        let sourceChars = source.split("");
        let tempChars = temp.split("");
        let flag = false;
        for (var i = 0; i < sourceChars.length; i++) {
            if (tempChars[i] != ' ') {
                if (i == 0) {
                    result.append("<span style='color:red'>");
                    result.append(sourceChars[i]);
                }
                else if (flag) {
                    result.append(sourceChars[i]);
                }
                else {
                    result.append("<span style='color:red'>");
                    result.append(sourceChars[i]);
                }
                flag = true;
                if (i == sourceChars.length - 1) {
                    result.append("</span>");
                }
            }
            else if (flag == true) {
                result.append("</span>");
                result.append(sourceChars[i]);
                flag = false;
            } else {
                result.append(sourceChars[i]);
            }
        }
        return result.toString();
    }
    return getHighLightDifferent(str1, str2)
}
// console.log(getHeightDiff("王五张三", "张三李四"))
