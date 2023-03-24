const sdpProdUrl = 'xxx'

export const isProd = () => sdpProdUrl == location.host

const XH_E_TOKEN_T = 'xh_e_token_t' // erp token 测试
const XH_C_TOKEN_T = 'xh_c_token_t' // common token 测试
const XH_E_TOKEN_P = 'xh_e_token_p' // erp token 生产
const XH_C_TOKEN_P = 'xh_c_token_p' // common token 生产
const XH_E_TOKEN_T_RDP = 'xh_e_token_t_rdp' // rdp erp token 测试
const XH_E_TOKEN_P_RDP = 'xh_e_token_p_rdp' // rdp erp token 生产
/**
 * 获取cookie项的值
 * @param {*} name cookie项的key
 * @returns
 */
export function getCookieItem (name) {
    var nameEQ = name + '='
    var ca = document.cookie.split(';')
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i]
        while (c.charAt(0) === ' ') {
            c = c.substring(1, c.length)
        }
        if (c.indexOf(nameEQ) === 0) {
            return c.substring(nameEQ.length, c.length)
        }
    }
    return null
}

/**
 * 设置domain为一级域名的cookie项
 * @param {*} name cookie项的key
 * @param {*} value cookie项的值
 * @param {} days 可选, 默认expires为''
 */
export function setCookieItem (name, value, days) {
    let domain, domainParts, date, expires, host
    if (days) {
        date = new Date()
        date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
        expires = '; expires=' + date.toGMTString()
    } else {
        expires = ''
    }
    host = location.host
    if (host.split('.').length === 1) {
        document.cookie = name + '=' + value + expires + '; path=/'
    } else {
        domainParts = host.split('.')
        domainParts.shift()
        domain = '.' + domainParts.join('.')
        document.cookie =
            name + '=' + value + expires + '; path=/; domain=' + domain
    }
}

/**
 *
 * @returns {
 *  e_token,
 *  c_token
 * }
 */
export const getCookieToken = () => {
    const prod = isProd()
    const name = prod ? XH_E_TOKEN_P : XH_E_TOKEN_T
    return {
        e_token: prod ? getCookieItem(XH_E_TOKEN_P) : getCookieItem(XH_E_TOKEN_T),
        c_token: prod ? getCookieItem(XH_C_TOKEN_P) : getCookieItem(XH_C_TOKEN_T),
        storage_e_token: sessionStorage.getItem(name)
    }
}

export const removeCookieToken = () => {
    const prod = isProd()
    const eTokenName = prod ? XH_E_TOKEN_P : XH_E_TOKEN_T
    const cTokenName = prod ? XH_C_TOKEN_P : XH_C_TOKEN_T
    const eTokenNameRdp = prod ? XH_E_TOKEN_P_RDP : XH_E_TOKEN_T_RDP
    setCookieItem(eTokenName, '', -1)
    setCookieItem(cTokenName, '', -1)
    setCookieItem(eTokenNameRdp, '', -1)
    setErpTokenStorage('')
}

export const setErpTokenCookie = (value) => {
    const prod = isProd()
    const name = prod ? XH_E_TOKEN_P : XH_E_TOKEN_T
    setCookieItem(name, value)
    if (value) {
        setErpTokenStorage(value)
    }
}

export const setCommonTokenCookie = (value) => {
    const prod = isProd()
    const name = prod ? XH_C_TOKEN_P : XH_C_TOKEN_T
    setCookieItem(name, value)
}

export const setErpTokenStorage = (value) => {
    const prod = isProd()
    const name = prod ? XH_E_TOKEN_P : XH_E_TOKEN_T
    sessionStorage.setItem(name, value)
}
