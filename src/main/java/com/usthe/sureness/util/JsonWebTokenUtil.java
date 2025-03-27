package com.usthe.sureness.util;

import com.usthe.sureness.processor.exception.ExtSurenessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.security.SignatureException;

import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.regex.Pattern;

/**
 * json web token util
 * use hmac algorithm, can change the secretKey by setDefaultSecretKey
 * @author tomsun28
 * @date 16:29 2018/3/8
 */
public class JsonWebTokenUtil {

    /** default SUBJECT KEY **/
    private static final String DEFAULT_SECRET_KEY =
            "MIIEowIBAl+f/dKhaX0csgOCTlCxq20yhmUea6H6JIpST3ST1SE2Rwp" +
            "LnfKefTjsIfJLBa2YkhEqE/GtcHDTNe4CU6+9y/S5z50Kik70LsP43r" +
            "RnLN7XNn4wARoQXizIv6MHUsIV+EFfiMw/x7R0ntu4aWr/CWuApcFaj" +
            "4mWEa6EwrPHTZmbT5Mt45AM2UYhzDHK+0F0rUq3MwH+oXsm+L3F/zjj" +
            "M6EByXIO+SV5+8tVt4bisXQ13rbN0oxhUZR73+LDj9mxa6rFhMW+lfx" +
            "CyaFv0bwq2Eik0jdrKUtsA6bx3sDJeFV643R+YYzGMRIqcBIp6AKA98" +
            "GM2RIqcBIp6-?::4390fsf4sdl6opf)4ZI:tdQMtcQQ14pkOAQdQ546";

    /** JWT format has 3 point **/
    private static final int COUNT_3 = 3;

    /** Determine whether it is a base64 string **/
    private static final Pattern BASE64_PATTERN =
            Pattern.compile("^([A-Za-z0-9+/_-]+)(=*)$");

    /** Encryption and decryption signature **/
    private static SecretKey secretKey;

    private static volatile boolean isUsedDefault = true;

    static {
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(DEFAULT_SECRET_KEY);
        secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }


    /**
     * issue json web token
     * @param id token ID
     * @param subject user ID
     * @param issuer issuer
     * @param period period time(s)
     * @param roles Access claim-roles
     * @param permissions Access claim-permissions
     * @param isRefresh is a refresh token
     * @return java.lang.String jwt
     */
    @Deprecated
    public static String issueJwt(String id, String subject, String issuer, Long period,
                                  List<String> roles, List<String> permissions,
                                  Boolean isRefresh) {
        Map<String, Object> customClaimMap = new HashMap<>(4);
        customClaimMap.put(SurenessConstant.ROLES, roles);
        customClaimMap.put("perms", permissions);
        customClaimMap.put("isRefresh", isRefresh);
        return issueJwtAll(id, subject, issuer, period, null, null,
                null, null, customClaimMap);
    }

    /**
     * issue json web token
     * @param id token ID
     * @param subject user ID
     * @param issuer issuer
     * @param period period time(s)
     * @param roles Access claim-roles
     * @return java.lang.String jwt
     */
    public static String issueJwt(String id, String subject, String issuer, Long period, List<String> roles) {
        Map<String, Object> customClaimMap = Collections.singletonMap(SurenessConstant.ROLES, roles);
        return issueJwtAll(id, subject, issuer, period, null, null,
                null, null, customClaimMap);
    }

    /**
     * issue all jwt params
     * @param id token ID
     * @param subject user ID
     * @param issuer issuer
     * @param period period time(s)
     * @param audience this ID Token is intended for, client id info
     * @param payload payload
     * @param notBefore Not Before(s)
     * @param roles roles the user has
     * @param headerMap header
     * @param customClaimMap custom claim param
     * @return json web token
     */
    public static String issueJwt(String id, String subject, String issuer, Long period,
                                  String audience, String payload, Long notBefore, List<String> roles,
                                  Map<String, Object> headerMap, Map<String, Object> customClaimMap){
        if (customClaimMap == null) {
            customClaimMap = Collections.singletonMap(SurenessConstant.ROLES, roles);
        } else {
            customClaimMap.put(SurenessConstant.ROLES, roles);
        }
        return issueJwtAll(id, subject, issuer, period, audience, payload, notBefore, headerMap, customClaimMap);
    }

    /**
     * issue all jwt params
     * @param id token ID
     * @param subject user ID
     * @param issuer issuer
     * @param period period time(s)
     * @param roles roles the user has
     * @param customClaimMap custom claim param
     * @return json web token
     */
    public static String issueJwt(String id, String subject, String issuer, Long period,
                                  List<String> roles, Map<String, Object> customClaimMap){
        if (customClaimMap == null) {
            customClaimMap = new HashMap<>(8);
        }
        if (roles != null && !roles.isEmpty()) {
            customClaimMap.put(SurenessConstant.ROLES, roles);
        }
        return issueJwtAll(id, subject, issuer, period, null, null,
                null, null, customClaimMap);
    }

    /**
     * issue jwt params
     * @param subject user ID
     * @param period period time(s)
     * @param roles roles the user has
     * @param customClaimMap custom claim param
     * @return json web token
     */
    public static String issueJwt(String subject, Long period,
                                  List<String> roles, Map<String, Object> customClaimMap){
        String id = UUID.randomUUID().toString();
        String issuer = "sureness-token-server";
        return issueJwt(id, subject, issuer, period,
                roles, customClaimMap);
    }

    /**
     * issue jwt params
     * @param subject user ID
     * @param period period time(s)
     * @param roles roles the user has
     * @return json web token
     */
    public static String issueJwt(String subject, Long period, List<String> roles){
        String id = UUID.randomUUID().toString();
        String issuer = "sureness-token-server";
        return issueJwt(id, subject, issuer, period,
                roles, null);
    }

    /**
     * issue jwt params
     * @param subject user ID
     * @param period period time(s)
     * @return json web token
     */
    public static String issueJwt(String subject, Long period){
        String id = UUID.randomUUID().toString();
        String issuer = "sureness-token-server";
        return issueJwt(id, subject, issuer, period,
                null, null);
    }

    /**
     * issue jwt params
     * @param subject user ID
     * @param period period time(s)
     * @param customClaimMap custom claim param
     * @return json web token
     */
    public static String issueJwt(String subject, Long period, Map<String, Object> customClaimMap){
        String id = UUID.randomUUID().toString();
        String issuer = "sureness-token-server";
        return issueJwt(id, subject, issuer, period, null, customClaimMap);
    }

    /**
     * issue all jwt params
     * @param id token ID
     * @param subject user ID
     * @param issuer issuer
     * @param period period time(s)
     * @param audience this ID Token is intended for, client id info
     * @param payload payload
     * @param notBefore Not Before(s)
     * @param headerMap header
     * @param customClaimMap custom claim param
     * @return json web token
     */
    public static String issueJwtAll(String id, String subject, String issuer, Long period,
                                     String audience, String payload, Long notBefore,
                                     Map<String, Object> headerMap, Map<String, Object> customClaimMap){
        if (isUsedDefault) {
            throw new ExtSurenessException("Please config your custom jwt secret. JsonWebTokenUtil.setDefaultSecretKey | sureness.jwt.secret");
        }
        long currentTimeMillis = System.currentTimeMillis();
        JwtBuilder jwtBuilder = Jwts.builder();
        if (id != null) {
            jwtBuilder.claim("id",id);
        }
        if (subject != null) {
            jwtBuilder.claim("subject",subject);
        }
        if (issuer != null) {
            jwtBuilder.claim("issuer",issuer);
        }
        // set issue create time
        jwtBuilder.claim("issuedAt",new Date(currentTimeMillis));
        // set expired time
        if (null != period) {
            jwtBuilder.claim("expiration",new Date(currentTimeMillis + period * 1000));
        }
        if (null != audience) {
            jwtBuilder.claim("audience",audience);
        }
        if (null != payload) {
            jwtBuilder.claim("payload",payload);
        }
        if (null != notBefore){
            jwtBuilder.claim("notBefore",new Date(notBefore * 1000));
        }
        if(null != headerMap) {
            jwtBuilder.claim("headerMap",headerMap);
        }
        //claim param, eg: roles, perms, isRefresh
        if (null != customClaimMap) {
            customClaimMap.forEach(jwtBuilder::claim);
        }
        // compress，optional GZIP
        jwtBuilder.compressWith( Jwts.ZIP.DEF);
        // set secret key
        jwtBuilder.signWith(secretKey);
        return jwtBuilder.compact();
    }

    /**
     * To determine whether it is not a JWT
     * Use format to judge, no verification
     * @param jwt JWT TOKEN
     * @return is a JWT return false, else true
     */
    public static boolean isNotJsonWebToken(String jwt) {
        if (jwt == null || "".equals(jwt)) {
            return true;
        }
        // base64url_encode(Header) + '.' + base64url_encode(Claims) + '.' + base64url_encode(Signature)
        String[] jwtArr = jwt.split("\\.");
        if (jwtArr.length != COUNT_3) {
            return true;
        }
        for (String jwtTmp : jwtArr) {
            if (!BASE64_PATTERN.matcher(jwtTmp).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param jwt json web token
     * @return parse content body
     * @throws ExpiredJwtException token expired
     * @throws UnsupportedJwtException unSupport TOKEN
     * @throws MalformedJwtException Parameter format exception
     * @throws SignatureException signature exception
     * @throws IllegalArgumentException illegal argument
     */
    public static Claims parseJwt(String jwt) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SignatureException, IllegalArgumentException {

        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(jwt).getPayload();

        // token ID -- claims.getId()
        // user ID -- claims.getSubject()
        // issuer -- claims.getIssuer()
        // issue time -- claims.getIssuedAt()
        // audience -- claims.getAudience()
        // Access claim-roles -- claims.get("roles", String.class)
        // Access claim-permissions -- claims.get("perms", String.class)
    }

    /**
     * set the jwt secret key
     * @param secretNowKeyValue key value
     */
    public static void setDefaultSecretKey(String secretNowKeyValue) {
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretNowKeyValue);
        secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        isUsedDefault = false;
    }
}
