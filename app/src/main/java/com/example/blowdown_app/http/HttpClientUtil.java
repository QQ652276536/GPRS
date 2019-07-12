package com.example.blowdown_app.http;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class HttpClientUtil {

    public HttpClientUtil() {
    }

    public String SendByPost(String url, Map<String, String> map) {
        //第一步:创建HttpClient对象
        HttpClient httpClient = new DefaultHttpClient();
        //第二步:创建代表请求的对象,参数是服务器地址
        HttpPost httpPost = new HttpPost(url);
        try {
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            JSONObject jsonParam = new JSONObject();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                jsonParam.put(entry.getKey().toString(), entry.getValue());
            }
            StringEntity stringEntity = new StringEntity(jsonParam.toString(), "utf-8");
            if (stringEntity != null) {
                //设置编码及请求参数类型
                stringEntity.setContentEncoding("utf-8");
                stringEntity.setContentType("application/json");
                httpPost.setEntity(stringEntity);
            }
            //第三步:执行请求,获取服务器返回内容
            HttpResponse httpResponse = httpClient.execute(httpPost);
            //第四步:检查相应状态是否正常
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //第五步:从对象中取出数据
                HttpEntity httpEntity = httpResponse.getEntity();
                return EntityUtils.toString(httpEntity, "utf-8");
            }
        } catch (JSONException | IOException e) {
            Log.e("LoginLog", e.toString());
            e.printStackTrace();
        }
        return null;
    }
}
