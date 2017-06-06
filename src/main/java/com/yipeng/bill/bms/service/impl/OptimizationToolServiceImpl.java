package com.yipeng.bill.bms.service.impl;

import com.yipeng.bill.bms.dao.BillMapper;
import com.yipeng.bill.bms.dao.ForbiddenWordsMapper;
import com.yipeng.bill.bms.dao.KeywordsPriceMapper;
import com.yipeng.bill.bms.domain.ForbiddenWords;
import com.yipeng.bill.bms.domain.KeywordsPrice;
import com.yipeng.bill.bms.model.Define;
import com.yipeng.bill.bms.model.Md5_UrlEncode;
import com.yipeng.bill.bms.service.OptimizationToolService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by 鱼在我这里。 on 2017/5/25.
 */
@Service
public class OptimizationToolServiceImpl implements OptimizationToolService {

    @Autowired
    private ForbiddenWordsMapper forbiddenWordsMapper;
    @Autowired
    private KeywordsPriceMapper keywordsPriceMapper;

    @Autowired
    private RemoteServiceImpl remoteService;
    @Autowired
    private BillMapper billMapper;
    Md5_UrlEncode md5_urlEncode = new Md5_UrlEncode();

    @Override
    public List<KeywordsPrice> forbiddenWordsList(String keywords) {
        if (keywords == "") {
            return null;
        }
        //获取所输入的关键字
        String[] arr = keywords.split("\n");
        String where = "";
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < arr.length; i++) {
            //清除空格
            arr[i] = arr[i].replace("\t", "").replace(" ", "");
            String aa = arr[i];
            where += " or words LIKE '" + arr[i] + "'";
            list.add(arr[i]);
        }
        //去除重复关键词
        HashSet h = new HashSet(list);
        list.clear();
        list.addAll(h);
        Map<String, Object> params = new HashMap<>();
        params.put("words", where);
        List<ForbiddenWords> forbiddenWordsList = forbiddenWordsMapper.selectByWords(params);
        for (ForbiddenWords item : forbiddenWordsList
                ) {
            list.remove(item.getWords());//移除违禁词
        }
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<KeywordsPrice> keywordsprices = keywordsPriceMapper.selectByword(list);//查询本地数据
        //if (!CollectionUtils.isEmpty(keywordsprices)) {
        for (KeywordsPrice item : keywordsprices
                ) {
            list.remove(item.getKeywords());
        }
        //是否还存在可调用接口集合
        if (!CollectionUtils.isEmpty(list)) {
            //调用接口
            long time = (long) (System.currentTimeMillis() / 1000);
            String action = "AddSearchTask";
            String keyword = "";
            for (String item : list
                    ) {
                keyword += "\"" + item + "\",";
            }
            //去掉最后面的逗号
            keyword = keyword.substring(0, keyword.length() - 1);
            String data = "{\"userId\":" + Define.userId + ",\"time\":" + time + ",\"businessType\":1008,\"keyword\":[" + keyword + "]}";
            String sign = "";
            try {
                //sign验证加密
                sign = md5_urlEncode.EncoderByMd5(action + Define.token + data).toUpperCase();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //调用接口的参数
            Map<String, String> priceMap = new HashMap<>();
            priceMap.put("wAction", action);
            priceMap.put("wSign", sign);
            priceMap.put("wParam", data);
            //调用接口方法
            String result = remoteService.getPriceApiId(priceMap);
            JSONObject myJsonObject = new JSONObject(result);
            JSONArray array = myJsonObject.getJSONArray("xValue");
            for (int i = 0; i < array.length(); i++) {
                KeywordsPrice keywordsPrice = new KeywordsPrice();
                keywordsPrice.setTaskid(Integer.parseInt(array.getJSONArray(i).get(0).toString()));
                keywordsPrice.setKeywords(list.get(i));
                keywordsprices.add(keywordsPrice);
                keywordsPriceMapper.insert(keywordsPrice);
            }

        }
        return keywordsprices;
    }

    @Override
    public Boolean LoopAllKeywords() {
        while (true) {
            List<String> keywords = billMapper.selectAllKeywords();
            if (CollectionUtils.isEmpty(keywords)) {
                break;
            }
            //调用接口
            long time = (long) (System.currentTimeMillis() / 1000);
            String action = "AddSearchTask";
            String keyword = "";
            for (String item : keywords
                    ) {
                keyword += "\"" + item + "\",";
            }
            //去掉最后面的逗号
            keyword = keyword.substring(0, keyword.length() - 1);
            String data = "{\"userId\":" + Define.userId + ",\"time\":" + time + ",\"businessType\":1008,\"keyword\":[" + keyword + "]}";
            String sign = "";
            try {
                //sign验证加密
                sign = md5_urlEncode.EncoderByMd5(action + Define.token + data).toUpperCase();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            //调用接口的参数
            Map<String, String> priceMap = new HashMap<>();
            priceMap.put("wAction", action);
            priceMap.put("wSign", sign);
            priceMap.put("wParam", data);
            //调用接口方法
            String result = remoteService.getPriceApiId(priceMap);
            JSONObject myJsonObject = new JSONObject(result);
            JSONArray array = myJsonObject.getJSONArray("xValue");
            for (int i = 0; i < array.length(); i++) {
                KeywordsPrice keywordsPrice = new KeywordsPrice();
                keywordsPrice.setTaskid(Integer.parseInt(array.getJSONArray(i).get(0).toString()));
                keywordsPrice.setKeywords(keywords.get(i));
                int num = keywordsPriceMapper.insert(keywordsPrice);
                if (num == 0)
                    return false;
            }
        }
        return true;
    }
}
