package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.model.SdpToken;
import com.chitu.bigdata.sdp.api.vo.TokenResp;
import com.chitu.bigdata.sdp.mapper.SdpTokenMapper;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/12 19:30
 */
@Service
@Slf4j
public class TokenService extends GenericService<SdpToken, Long> {

    public TokenService(@Autowired SdpTokenMapper tokenMapper) {
        super(tokenMapper);
    }

    @Autowired
    private SdpTokenMapper tokenMapper;
    @Autowired
    TokenService tokenService;

    private final static Long EXPIRE = 3600 * 1L;

    @Deprecated
    public TokenResp createToken(Long userId,String token) throws Exception{
//        String token = TokenGenerator.generateValue();
        SdpToken token1 = new SdpToken();
        token1.setUserId(userId);
        List<SdpToken> tokenList = selectAll(token1);

        Date now = new Date();
        Date expireTime = new Date(now.getTime() + EXPIRE * 1000);
        if (CollectionUtils.isEmpty(tokenList)) {
            Context context = ContextUtils.get();
            SdpToken sdpToken = new SdpToken();
            sdpToken.setUserId(userId);
            sdpToken.setToken(token);
            sdpToken.setExpireTime(new Timestamp(expireTime.getTime()));
            if(context != null){
                sdpToken.setCreatedBy(context.getUserId());
                sdpToken.setUpdatedBy(context.getUserId());
            }
            sdpToken.setCreationDate(new Timestamp(System.currentTimeMillis()));
            sdpToken.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            // 保存token
            tokenMapper.insert(sdpToken);
        } else {
            SdpToken userToken = tokenList.stream().max(Comparator.comparing(SdpToken::getCreationDate)).get();
            userToken.setToken(token);
            userToken.setExpireTime(new Timestamp(expireTime.getTime()));
            //更新token
            tokenService.updateSelective(userToken);
        }

        TokenResp tokenResp = new TokenResp();
        tokenResp.setToken(token);
        tokenResp.setExpire(EXPIRE);
        return tokenResp;
    }
}
