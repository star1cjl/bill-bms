package com.yipeng.bill.bms.service.impl;

import com.yipeng.bill.bms.dao.*;
import com.yipeng.bill.bms.domain.*;
import com.yipeng.bill.bms.service.HomeService;
import com.yipeng.bill.bms.service.RankingUpdateService;
import com.yipeng.bill.bms.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/4/10.
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private BillCostMapper billCostMapper;
    @Autowired
    private BillMapper billMapper;
    @Autowired
    private FundAccountMapper fundAccountMapper;
    @Autowired
    private BillPriceMapper billPriceMapper;
    @Autowired
    private SearchenginecompletionrateMapper searchenginecompletionrateMapper;
    @Autowired
    private  noticepublishMapper noticepublishMapper;

    /**
     * 首页详情
     * @param loginUser
     * @return
     */
    @Override
    public Map<String, Object> homeDetails(LoginUser loginUser) {
        //转换时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM");
        String daynow=formatter.format(new Date());
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        //当前时间
        Date dateNow=null;
        try {
            dateNow = formatter1.parse(daynow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //获取上一个月
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateNow); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
        //上一个月的日期
        Date preMonth=null;
        try {
            preMonth = format1.parse( formatter.format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //上一个月的天数
        int monthPreCount=0;
        monthPreCount=getDaysOfMonth(preMonth);

        calendar.setTime(new Date());
        int monthNowCount = calendar.get(Calendar.DAY_OF_MONTH);
        String seriesNowMonth="";
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(dateNow); // 设置为当前时间
        calendar1.set(Calendar.MONTH, calendar1.get(Calendar.MONTH)+1); // 设置为下一个月
        //下一个月的日期
        Date nextMonth=null;
        try {
            nextMonth = format1.parse( formatter.format(calendar1.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Map<String, String> mapss = getFirstday_Lastday_Month(nextMonth);
        String fistDaysNow=mapss.get("first");
        //将第一日转换为Date格式
        Date fistDateNow=null;
        try {
            fistDateNow = format1.parse(fistDaysNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Map<String, Object> map=new HashMap<>();
        //查询今日的搜索引擎完成率
        Map<String, Object> searchMap=new HashMap<>();//搜索引擎完成率查询对象
        Calendar now = Calendar.getInstance();
        searchMap.put("year",now.get(Calendar.YEAR));
        searchMap.put("month",now.get(Calendar.MONTH)+1);
        searchMap.put("day",now.get(Calendar.DATE));
        if(loginUser.hasRole("ASSISTANT"))
        {
            searchMap.put("userId",loginUser.getCreateUserId());
        }
        else
        {
            searchMap.put("userId",loginUser.getId());
        }

        Searchenginecompletionrate searchenginecompletionrate=searchenginecompletionrateMapper.selectByUsedIdAndDay(searchMap);
        if(searchenginecompletionrate!=null)
        {
            map.put("search",searchenginecompletionrate);
        }
        else {
            searchMap.put("day",now.get(Calendar.DATE)-1);
            Searchenginecompletionrate searchenginecompletionrateLast=searchenginecompletionrateMapper.selectByUsedIdAndDay(searchMap);
            if(searchenginecompletionrateLast!=null)
            {
                map.put("search",searchenginecompletionrateLast);
            }
            else
            {
                Searchenginecompletionrate searchenginecompletionrateNull=new Searchenginecompletionrate();
                searchenginecompletionrateNull.setAllcompleteness(0.00);
                searchenginecompletionrateNull.setBaiducompleteness(0.00);
                searchenginecompletionrateNull.setBaiduwapcompleteness(0.00);
                searchenginecompletionrateNull.setSanliulingcompleteness(0.00);
                searchenginecompletionrateNull.setShenmacompleteness(0.00);
                searchenginecompletionrateNull.setSougoucompleteness(0.00);
                map.put("search",searchenginecompletionrateNull);
            }
        }

        Map<String, Object> params=new HashMap<>();
        params.put("userId",loginUser.getId());
        if(loginUser.hasRole("SUPER_ADMIN"))
        {

            //某个月的第一天和最后一天
            Map<String, String> maps = getFirstday_Lastday_Month(dateNow);
            String fistDay=maps.get("first");
            //将第一日转换为Date格式
            Date fistDate=null;
            try {
                fistDate = format1.parse(fistDay);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String seriesLastMonth="";
            //上个月消费
            String seriesLastMonthSum="";
            //循环获取上个月每天的达标数
            Map<String,Object> dateMap=new  HashMap<>();
            dateMap.put("userId",loginUser.getId());
            //判断Y轴任务数的显示
            int MaxYbylast=0;
            //判断Y轴消费的显示
            double MaxYbylastCost=0;
            Map<String,Object> sqlMap=new HashMap<>();
            sqlMap.put("dateArr",formatter1.format(preMonth));
            sqlMap.put("userId",loginUser.getId());
            List<Map<String,Object>> lastSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMap);
            List<Map<String,Object>> lastCount=billCostMapper.selectByBillCostOfDayGroupBy(sqlMap);
            if(lastSum.size()>0)
            {
                int jianfaDate=monthPreCount-lastSum.size();
                if(jianfaDate>0)
                {
                    for(int i=0;i<jianfaDate;i++)
                    {
                        seriesLastMonthSum+=0+",";
                        seriesLastMonth+=0+",";
                    }
                }
                for(int i=0;i<lastSum.size();i++)
                {
                    String sqlLastSum=lastSum.get(i).get("costSum").toString();
                    String sqlLastCount=lastCount.get(i).get("costCount").toString();
                    //比较达标最大数
                    if(MaxYbylast<=Integer.parseInt(sqlLastCount))
                    {
                        MaxYbylast=Integer.parseInt(sqlLastCount);
                    }
                    //比较消费最大数
                    if(MaxYbylastCost<=Double.parseDouble(sqlLastSum))
                    {
                        MaxYbylastCost=Double.parseDouble(sqlLastSum);
                    }
                    seriesLastMonth+=sqlLastCount+",";
                    seriesLastMonthSum+=sqlLastSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthPreCount;i++)
                {
                    seriesLastMonth+=0.0+",";
                    seriesLastMonthSum+=0.0+",";
                }
            }
            //上个月的达标数
            map.put("seriesLastMonth",seriesLastMonth);
            map.put("seriesLastMonthSum",seriesLastMonthSum);
            //判断Y轴的显示
            int MaxYbyNew=0;
            double MaxYbyNewCost=0;
            String seriesNowMonthSum="";
            Map<String,Object> sqlMapNew=new HashMap<>();
            sqlMapNew.put("dateArr",formatter1.format(new Date()));
            sqlMapNew.put("userId",loginUser.getId());
            List<Map<String,Object>> newSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMapNew);
            List<Map<String,Object>> newCount=billCostMapper.selectByBillCostOfDayGroupBy(sqlMapNew);

            if(newCount.size()<monthNowCount)
            {
                for(int i=0;i<monthNowCount-1;i++)
                {
                    String sqlNewSum=newSum.get(i).get("costSum").toString();
                    String sqlNewCount=newCount.get(i).get("costCount").toString();
                    //比较达标最大数
                    if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                    {
                        MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                    }
                    if(MaxYbyNew<=Integer.parseInt(sqlNewCount))
                    {
                        MaxYbyNew=Integer.parseInt(sqlNewCount);
                    }
                    seriesNowMonth+=sqlNewCount+",";
                    seriesNowMonthSum+=sqlNewSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthNowCount;i++)
                {
                    String sqlNewSum=newSum.get(i).get("costSum").toString();
                    String sqlNewCount=newCount.get(i).get("costCount").toString();
                    //比较达标最大数
                    if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                    {
                        MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                    }
                    if(MaxYbyNew<=Integer.parseInt(sqlNewCount))
                    {
                        MaxYbyNew=Integer.parseInt(sqlNewCount);
                    }
                    seriesNowMonth+=sqlNewCount+",";
                    seriesNowMonthSum+=sqlNewSum+",";

                }
            }
            map.put("seriesNowMonth",seriesNowMonth);
            map.put("seriesNowMonthSum",seriesNowMonthSum);
            String yAxis="";
            if(MaxYbyNew!=0)
            {
                yAxis=getYAxis(MaxYbyNew);
            }
            else
            {
                yAxis=getYAxis(MaxYbylast);
            }
            String yAxisSum="";
            if(MaxYbyNewCost!=0)
            {
                yAxisSum=getYAxisSum(MaxYbyNewCost);
            }
            else
            {
                yAxisSum=getYAxisSum(MaxYbylastCost);
            }
            map.put("yAxis",yAxis);
            map.put("yAxisSum",yAxisSum);

            return map;
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            //某个月的第一天和最后一天
            Map<String, String> maps = getFirstday_Lastday_Month(dateNow);
            String fistDay=maps.get("first");
            //将第一日转换为Date格式
            Date fistDate=null;
            try {
                fistDate = format1.parse(fistDay);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //上个月达标数
            String seriesLastMonth="";
            //上个月消费
            String seriesLastMonthSum="";
            //判断Y轴的显示
            int MaxYbylast=0;
            //判断Y轴消费的显示
            double MaxYbylastCost=0;
            Map<String,Object> sqlMap=new HashMap<>();
            sqlMap.put("dateArr",formatter1.format(preMonth));
            if(loginUser.hasRole("ASSISTANT"))
            {
                sqlMap.put("userId",loginUser.getCreateUserId());
            }
            else
            {
                sqlMap.put("userId",loginUser.getId());
            }

            List<Map<String,Object>> lastSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMap);
            if(lastSum.size()>0)
            {
                int jianfaDate=monthPreCount-lastSum.size();
                if(jianfaDate>0)
                {
                    for(int i=0;i<jianfaDate;i++)
                    {
                        seriesLastMonthSum+=0+",";
                    }
                }
                for(int i=0;i<lastSum.size();i++)
                {
                    String sqlLastSum=lastSum.get(i).get("costSum").toString();
                    //比较消费最大数
                    if(MaxYbylastCost<=Double.parseDouble(sqlLastSum))
                    {
                        MaxYbylastCost=Double.parseDouble(sqlLastSum);
                    }

                    seriesLastMonthSum+=sqlLastSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthPreCount;i++)
                {
                    seriesLastMonthSum+=0+",";
                }
            }
            //上个月的达标数
            map.put("seriesLastMonthSum",seriesLastMonthSum);
            //判断Y轴的显示
            int MaxYbyNew=0;
            double MaxYbyNewCost=0;
            String seriesNowMonthSum="";
            Map<String,Object> sqlMapNew=new HashMap<>();
            sqlMapNew.put("dateArr",formatter1.format(new Date()));
            if(loginUser.hasRole("ASSISTANT"))
            {
                sqlMapNew.put("userId",loginUser.getCreateUserId());
            }
            else
            {
                sqlMapNew.put("userId",loginUser.getId());
            }
            List<Map<String,Object>> newSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMapNew);
            if(newSum.size()<monthNowCount)
            {
                if(!CollectionUtils.isEmpty(newSum))
                {
                    for(int i=0;i<monthNowCount-1;i++)
                    {
                        String sqlNewSum=newSum.get(i).get("costSum").toString();
                        //比较达标最大数
                        if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                        {
                            MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                        }

                        seriesNowMonthSum+=sqlNewSum+",";
                    }
                }

            }
            else
            {
                for(int i=0;i<monthNowCount;i++)
                {
                    String sqlNewSum=newSum.get(i).get("costSum").toString();
                    //比较达标最大数
                    if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                    {
                        MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                    }

                    seriesNowMonthSum+=sqlNewSum+",";

                }
            }
            String yAxis="";
            if(MaxYbyNew!=0)
            {
                yAxis=getYAxis(MaxYbyNew);
            }
            else
            {
                yAxis=getYAxis(MaxYbylast);
            }
            String yAxisSum="";
            if(MaxYbyNewCost!=0)
            {
                yAxisSum=getYAxisSum(MaxYbyNewCost);
            }
            else
            {
                yAxisSum=getYAxisSum(MaxYbylastCost);
            }
            map.put("yAxisSum",yAxisSum);
            map.put("seriesNowMonthSum",seriesNowMonthSum);
            return map;
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {

            //某个月的第一天和最后一天
            Map<String, String> maps = getFirstday_Lastday_Month(dateNow);
            String fistDay=maps.get("first");
            //将第一日转换为Date格式
            Date fistDate=null;
            try {
                fistDate = format1.parse(fistDay);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String seriesLastMonth="";
            //上个月消费
            String seriesLastMonthSum="";
            //判断Y轴的显示
            int MaxYbylast=0;
            //判断Y轴消费的显示
            double MaxYbylastCost=0;
            Map<String,Object> sqlMap=new HashMap<>();
            sqlMap.put("dateArr",formatter1.format(preMonth));
            sqlMap.put("userId",loginUser.getCreateUserId());
            sqlMap.put("billAscription",loginUser.getId());
            List<Map<String,Object>> lastSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMap);
            if(lastSum.size()>0)
            {
                int jianfaDate=monthPreCount-lastSum.size();
                if(jianfaDate>0)
                {
                    for(int i=0;i<jianfaDate;i++)
                    {
                        seriesLastMonthSum+=0+",";
                    }
                }
                for(int i=0;i<lastSum.size();i++)
                {
                    String sqlLastSum=lastSum.get(i).get("costSum").toString();
                    //比较消费最大数
                    if(MaxYbylastCost<=Double.parseDouble(sqlLastSum))
                    {
                        MaxYbylastCost=Double.parseDouble(sqlLastSum);
                    }

                    seriesLastMonthSum+=sqlLastSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthPreCount;i++)
                {
                    seriesLastMonthSum+=0+",";
                }
            }

            map.put("seriesLastMonthSum",seriesLastMonthSum);

            //判断Y轴的显示
            int MaxYbyNew=0;
            double MaxYbyNewCost=0;
            String seriesNowMonthSum="";
            Map<String,Object> sqlMapNew=new HashMap<>();
            sqlMapNew.put("dateArr",formatter1.format(new Date()));
            sqlMapNew.put("userId",loginUser.getCreateUserId());
            sqlMapNew.put("billAscription",loginUser.getId());
            List<Map<String,Object>> newSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMapNew);
            if(newSum.size()<monthNowCount)
            {
                for(int i=0;i<monthNowCount-1;i++)
                {
                    String sqlNewSum=newSum.get(i).get("costSum").toString();
                    //比较达标最大数
                    if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                    {
                        MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                    }

                    seriesNowMonthSum+=sqlNewSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthNowCount;i++)
                {
                    String sqlNewSum=newSum.get(i).get("costSum").toString();
                    //比较达标最大数
                    if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                    {
                        MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                    }

                    seriesNowMonthSum+=sqlNewSum+",";

                }
            }
            String yAxis="";
            if(MaxYbyNew!=0)
            {
                yAxis=getYAxis(MaxYbyNew);
            }
            else
            {
                yAxis=getYAxis(MaxYbylast);
            }
            String yAxisSum="";
            if(MaxYbyNewCost!=0)
            {
                yAxisSum=getYAxisSum(MaxYbyNewCost);
            }
            else
            {
                yAxisSum=getYAxisSum(MaxYbylastCost);
            }

            map.put("yAxisSum",yAxisSum);
            map.put("seriesNowMonthSum",seriesNowMonthSum);
            return map;
        }
        else if (loginUser.hasRole("CUSTOMER"))
        {

            //某个月的第一天和最后一天
            Map<String, String> maps = getFirstday_Lastday_Month(dateNow);
            String fistDay=maps.get("first");
            //将第一日转换为Date格式
            Date fistDate=null;
            try {
                fistDate = format1.parse(fistDay);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String seriesLastMonth="";
            //上个月消费
            String seriesLastMonthSum="";
            //判断Y轴的显示
            int MaxYbylast=0;
            //判断Y轴消费的显示
            double MaxYbylastCost=0;
            Map<String,Object> sqlMap=new HashMap<>();
            sqlMap.put("dateArr",formatter1.format(preMonth));
            sqlMap.put("outMemberId",loginUser.getId());
            List<Map<String,Object>> lastSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMap);
            if(lastSum.size()>0)
            {
                int jianfaDate=monthPreCount-lastSum.size();
                if(jianfaDate>0)
                {
                    for(int i=0;i<jianfaDate;i++)
                    {
                        seriesLastMonthSum+=0+",";
                    }
                }
                for(int i=0;i<lastSum.size();i++)
                {
                    String sqlLastSum=lastSum.get(i).get("costSum").toString();
                    //比较消费最大数
                    if(MaxYbylastCost<=Double.parseDouble(sqlLastSum))
                    {
                        MaxYbylastCost=Double.parseDouble(sqlLastSum);
                    }

                    seriesLastMonthSum+=sqlLastSum+",";
                }
            }
            else
            {
                for(int i=0;i<monthPreCount;i++)
                {
                    seriesLastMonthSum+=0+",";
                }
            }
            //上个月的达标数
            map.put("seriesLastMonthSum",seriesLastMonthSum);
            //判断Y轴的显示
            //判断Y轴的显示
            int MaxYbyNew=0;
            double MaxYbyNewCost=0;
            String seriesNowMonthSum="";
            Map<String,Object> sqlMapNew=new HashMap<>();
            sqlMapNew.put("dateArr",formatter1.format(new Date())); ;
            sqlMapNew.put("outMemberId",loginUser.getId());
            List<Map<String,Object>> newSum=billCostMapper.selectByBillCostOfDaySumGroupBy(sqlMapNew);
            if(!CollectionUtils.isEmpty(newSum))
            {
                if(newSum.size()<monthNowCount)
                {
                    for(int i=0;i<monthNowCount-1;i++)
                    {
                        String sqlNewSum=newSum.get(i).get("costSum").toString();
                        //比较达标最大数
                        if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                        {
                            MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                        }

                        seriesNowMonthSum+=sqlNewSum+",";
                    }
                }
                else
                {
                    for(int i=0;i<monthNowCount;i++)
                    {
                        String sqlNewSum=newSum.get(i).get("costSum").toString();
                        //比较达标最大数
                        if(MaxYbyNewCost<=Double.parseDouble(sqlNewSum))
                        {
                            MaxYbyNewCost=Double.parseDouble(sqlNewSum);
                        }

                        seriesNowMonthSum+=sqlNewSum+",";

                    }
                }
            }

            String yAxis="";
            if(MaxYbyNew!=0)
            {
                yAxis=getYAxis(MaxYbyNew);
            }
            else
            {
                yAxis=getYAxis(MaxYbylast);
            }
            String yAxisSum="";
            if(MaxYbyNewCost!=0)
            {
                yAxisSum=getYAxisSum(MaxYbyNewCost);
            }
            else
            {
                yAxisSum=getYAxisSum(MaxYbylastCost);
            }


            map.put("yAxis",yAxis);
            map.put("yAxisSum",yAxisSum);
            map.put("seriesNowMonthSum",seriesNowMonthSum);
            return map;
        }

        return null;
    }

    //客户数
    @Override
    public Map<String, Object> userCount(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();//视图返回对象
        Map<String, Object> params=new HashMap<>();//查询参数对象
        params.put("userId",loginUser.getId());
        if(loginUser.hasRole("SUPER_ADMIN")) {
            //客户数
            Role role=roleMapper.selectByRoleCode("DISTRIBUTOR");
            params.put("roleId",role.getId());

            Long count=UserCount(params);
            map.put("UserCount",count);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER")) {
            params.put("ascription",loginUser.getId());
            params.put("inMemberId",loginUser.getCreateUserId());
            Long UserCount=userMapper.getUserBillAscriptionCount(params);
            map.put("UserCount",UserCount);

        }
        else {
            if (loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }
            Long count=UserCount(params);
            map.put("UserCount",count);
        }
        return map;
    }

    @Override
    public Map<String, Object> balance(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();//视图返回对象
        //账户余额
        FundAccount fundAccount=fundAccountMapper.selectByUserId(loginUser.getId());
        if (fundAccount==null)
        {

            map.put("balance",0);
        }
        else
        {
            map.put("balance",fundAccount.getBalance());

        }
        return map;
    }

    @Override
    public Map<String, Object> AllConsumption(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();//视图返回对象
        Map<String, Object> params=new HashMap<>();//查询参数对象
        params.put("userId",loginUser.getId());
        if(loginUser.hasRole("SUPER_ADMIN")) {
            //本月消费
            Double AllConsumption=AllConsumption(params);
            map.put("AllConsumption",AllConsumption);
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT")) {

            if(loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }
            //月总消费
            Double AllConsumption=AllConsumption(params);
            map.put("AllConsumption",AllConsumption);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            Calendar now =Calendar.getInstance();
            params.put("createId",loginUser.getCreateUserId());
            params.put("ascription",loginUser.getId());
            params.put("inMemberId",loginUser.getCreateUserId());
            Double sum=billCostMapper.MonthConsumptionCommissioner(params);
            map.put("AllConsumption",sum);
        }
        else
        {
            Calendar now =Calendar.getInstance();
            Double sum=billCostMapper.MonthConsumptionCustomer(params);
            map.put("AllConsumption",sum);
        }
        return map;
    }

    /**
     * 本月消费
     * @param loginUser
     * @return
     */
    @Override
    public Map<String, Object> MonthConsumption(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();//视图返回对象
        Map<String, Object> params=new HashMap<>();//查询参数对象
        params.put("userId",loginUser.getId());
        if(loginUser.hasRole("SUPER_ADMIN")) {
            //本月消费
            params.put("billType",1);
            Double MonthConsumption=MonthConsumption(params);
            map.put("MonthConsumption",MonthConsumption);
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT")) {
            if(loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }
            //月总消费
            Double MonthConsumption=MonthConsumption(params);
            map.put("MonthConsumption",MonthConsumption);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            Calendar now =Calendar.getInstance();
            params.put("year",now.get(Calendar.YEAR));
            params.put("month",now.get(Calendar.MONTH)+1);
            params.put("createId",loginUser.getCreateUserId());
            params.put("ascription",loginUser.getId());
            params.put("inMemberId",loginUser.getCreateUserId());
            Double sum=billCostMapper.MonthConsumptionCommissioner(params);
            map.put("MonthConsumption",sum);
        }
        else
        {
            Calendar now =Calendar.getInstance();
            params.put("year",now.get(Calendar.YEAR));
            params.put("month",now.get(Calendar.MONTH)+1);
            Double sum=billCostMapper.MonthConsumptionCustomer(params);
            map.put("MonthConsumption",sum);
        }
        return map;
    }
    //本日消费
    @Override
    public Map<String, Object> DayConsumption(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        Map<String, Object> params=new HashMap<>();
        Calendar now =Calendar.getInstance();
        params.put("year",now.get(Calendar.YEAR));
        params.put("month",now.get(Calendar.MONTH)+1);
        params.put("day",now.get(Calendar.DATE));
        params.put("userId",loginUser.getId());
        if(loginUser.hasRole("SUPER_ADMIN"))
        {
            params.put("billType",1);
            Double DayConsumption=DayConsumption(params);
            map.put("DayConsumption",DayConsumption);
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            if(loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }
            Double DayConsumption=DayConsumption(params);
            map.put("DayConsumption",DayConsumption);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            params.put("ascription",loginUser.getId());
            params.put("inMemberId",loginUser.getCreateUserId());
            params.put("createId",loginUser.getCreateUserId());
            Double sum1=billCostMapper.MonthConsumptionCommissioner(params);
            map.put("DayConsumption",sum1);

        }
        else if (loginUser.hasRole("CUSTOMER"))
        {
            Double sum1=billCostMapper.MonthConsumptionCustomer(params);
            map.put("DayConsumption",sum1);
        }


        return map;
    }
    //任务数
    @Override
    public Map<String, Object> billCount(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        Map<String, Object> params=new HashMap<>();
        params.put("userId",loginUser.getId());
        params.put("state",2);
        if(loginUser.hasRole("SUPER_ADMIN"))
        {
            Long billCount=billMapper.getBillListCount(params);
            map.put("billCount",billCount);
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {
            if(loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }

            Long billCount=billMapper.getBillListCount(params);
            map.put("billCount",billCount);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            params.put("userId",loginUser.getCreateUserId());
            params.put("billAscription",loginUser.getId());
            Long billCount=billMapper.getBillListCount(params);
            map.put("billCount",billCount);
        }
        else if (loginUser.hasRole("CUSTOMER")) {
            Long billCount=billMapper.getBillListByCmmCount(params);
            map.put("billCount",billCount);
        }
        return map;
    }
    //累计任务数
    @Override
    public Map<String, Object> AllbillCount(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        Map<String, Object> params=new HashMap<>();
        params.put("userId",loginUser.getId());
        params.put("state",2);
        params.put("state2",3);
        if(loginUser.hasRole("SUPER_ADMIN"))
        {
            Long billCount=billMapper.getBillListCount(params);
            map.put("AllbillCount",billCount);
        }
        //渠道商和代理商
        else if(loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            if(loginUser.hasRole("ASSISTANT"))
            {
                params.put("userId",loginUser.getCreateUserId());
            }
            Long billCount=billMapper.getBillListCount(params);
            map.put("AllbillCount",billCount);
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            params.put("userId",loginUser.getCreateUserId());
            params.put("billAscription",loginUser.getId());
            Long billCount=billMapper.getBillListCount(params);
            map.put("AllbillCount",billCount);
        }
        else if (loginUser.hasRole("CUSTOMER")) {
            Long billCount=billMapper.getBillListByCmmCount(params);
            map.put("AllbillCount",billCount);
        }
        return map;
    }
    //今日达标数
    @Override
    public Map<String, Object> standardSum(LoginUser loginUser) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        Map<String, Object> map=new HashMap<>();
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {
            //1,先获取对应所有的订单
            Map<String,Object> billListparams=new HashMap<>();
            if(loginUser.hasRole("ASSISTANT"))
            {
                billListparams.put("userId",loginUser.getCreateUserId());
            }
            else
            {
                billListparams.put("userId",loginUser.getId());
            }
            billListparams.put("state",2);
            //订单数（优化中）
            List<Bill> billList=billMapper.selectByInMemberId(billListparams);
            Map<String,Object> billparam=new HashMap<>();
            if(loginUser.hasRole("ASSISTANT"))
            {
                billparam.put("userId",loginUser.getCreateUserId());
            }
            else
            {
                billparam.put("userId",loginUser.getId());
            }
            Date tomorrow = calendar.getTime();
            String str1=formatter.format(tomorrow);
            billparam.put("date",str1);
            int keywordsCount=billCostMapper.selectByBillCostOfDay(billparam);
            map.put("standardSum",keywordsCount);

            //总完成率(今日达标数/总订单数)
            double AllCompleteness=0;
            if(billList.size()>0)
            {
                AllCompleteness=((double)keywordsCount/billList.size())*100;
            }
            else
            {
                AllCompleteness=0.00;
            }
            map.put("AllCompleteness",df.format(AllCompleteness));

        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            //1,先获取对应所有的订单
            Map<String,Object> billparam=new HashMap<>();
            billparam.put("userId",loginUser.getCreateUserId());
            billparam.put("billAscription",loginUser.getId());
            billparam.put("state",2);
            List<Bill> billList=billMapper.selectByInMemberId(billparam);
            Date tomorrow = calendar.getTime();
            String str1=formatter.format(tomorrow);
            billparam.put("date",str1);
            int keywordsCount=billCostMapper.selectByBillCostOfDay(billparam);
            map.put("standardSum",keywordsCount);
            //总完成率(今日达标数/总订单数)
            double AllCompleteness=0;
            if(billList.size()>0)
            {
                AllCompleteness=((double)keywordsCount/billList.size())*100;
            }
            else
            {
                AllCompleteness=0.00;
            }
            map.put("AllCompleteness",df.format(AllCompleteness));
        }
        else if (loginUser.hasRole("CUSTOMER"))
        {
            Map<String,Object> billListparams=new HashMap<>();
            billListparams.put("state",2);
            billListparams.put("userId",loginUser.getId());
            List<Bill> billList=billMapper.getBillCountByCmm(billListparams);

            Map<String,Object> dateMap=new  HashMap<>();
            dateMap.put("outMemberId",loginUser.getId());
            Date tomorrow = calendar.getTime();
            String str1=formatter.format(tomorrow);
            dateMap.put("date",str1);
            int keywordsCount=billCostMapper.selectByBillCostOfDay(dateMap);
            map.put("standardSum",keywordsCount);
            //总完成率(今日达标数/总订单数)
            double AllCompleteness=0;
            if(billList.size()>0)
            {
                AllCompleteness=((double)keywordsCount/billList.size())*100;
            }
            else
            {
                AllCompleteness=0.00;
            }
            map.put("AllCompleteness",df.format(AllCompleteness));

        }
        return map;
    }
    //百度完成率
    @Override
    public Map<String, Object> baiduCompleteness(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String baidu="百度";
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            Double baiduCompleteness=searchCompleteness(baidu,loginUser);
            map.put("baiduCompleteness",df.format(baiduCompleteness));
        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {

            Double baiduCompleteness=searchCompletenessBycmm(baidu,loginUser);
            map.put("baiduCompleteness",df.format(baiduCompleteness));
        }
        else if (loginUser.hasRole("CUSTOMER"))
        {

            Double baiduCompleteness=searchCompletenessBycus(baidu,loginUser);
            map.put("baiduCompleteness",df.format(baiduCompleteness));
        }
        return map;
    }
    //手机百度完成率
    @Override
    public Map<String, Object> baiduWapCompleteness(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String baiduWap="手机百度";
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            Double baiduWapCompleteness=searchCompleteness(baiduWap,loginUser);
            map.put("baiduWapCompleteness",df.format(baiduWapCompleteness));

        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {

            Double baiduWapCompleteness=searchCompletenessBycmm(baiduWap,loginUser);
            map.put("baiduWapCompleteness",df.format(baiduWapCompleteness));

        }
        else if (loginUser.hasRole("CUSTOMER"))
        {

            Double baiduWapCompleteness=searchCompletenessBycus(baiduWap,loginUser);
            map.put("baiduWapCompleteness",df.format(baiduWapCompleteness));
        }
        return map;
    }
    //360
    @Override
    public Map<String, Object> sanliulingCompleteness(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String sanliuling="360";
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            Double sanliulingCompleteness=searchCompleteness(sanliuling,loginUser);
            map.put("sanliulingCompleteness",df.format(sanliulingCompleteness));

        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {

            Double sanliulingCompleteness=searchCompletenessBycmm(sanliuling,loginUser);
            map.put("sanliulingCompleteness",df.format(sanliulingCompleteness));;

        }
        else if (loginUser.hasRole("CUSTOMER"))
        {

            Double sanliulingCompleteness=searchCompletenessBycus(sanliuling,loginUser);
            map.put("sanliulingCompleteness",df.format(sanliulingCompleteness));
        }
        return map;
    }
    //搜狗
    @Override
    public Map<String, Object> sougouCompleteness(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String sougou="搜狗";
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            Double sougouCompleteness=searchCompleteness(sougou,loginUser);
            map.put("sougouCompleteness",df.format(sougouCompleteness));

        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            Double sougouCompleteness=searchCompletenessBycmm(sougou,loginUser);
            map.put("sougouCompleteness",df.format(sougouCompleteness));

        }
        else if (loginUser.hasRole("CUSTOMER"))
        {
            Double sougouCompleteness=searchCompletenessBycus(sougou,loginUser);
            map.put("sougouCompleteness",df.format(sougouCompleteness));
        }
        return map;
    }
    //神马
    @Override
    public Map<String, Object> shenmaCompleteness(LoginUser loginUser) {
        Map<String, Object> map=new HashMap<>();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String shenma="神马";
        if(loginUser.hasRole("SUPER_ADMIN")||loginUser.hasRole("DISTRIBUTOR")||loginUser.hasRole("AGENT")||loginUser.hasRole("ASSISTANT"))
        {

            Double shenmaCompleteness=searchCompleteness(shenma,loginUser);
            map.put("shenmaCompleteness",df.format(shenmaCompleteness));

        }
        //操作员
        else  if (loginUser.hasRole("COMMISSIONER"))
        {
            Double shenmaCompleteness=searchCompletenessBycmm(shenma,loginUser);
            map.put("shenmaCompleteness",df.format(shenmaCompleteness));

        }
        else if (loginUser.hasRole("CUSTOMER"))
        {
            Double shenmaCompleteness=searchCompleteness(shenma,loginUser);
            map.put("shenmaCompleteness",df.format(shenmaCompleteness));
        }
        return map;
    }
    //客户数
    public Long UserCount(Map<String, Object> params)

    {
        Long Count=userMapper.getUserRoleByCreateIdCount(params);
        return  Count;
    }

    //总消费
    public  Double AllConsumption(Map<String, Object> params)
    {
        Calendar now =Calendar.getInstance();
        Double sum=billCostMapper.MonthConsumption(params);
        return sum;
    }
    //本月总消费
    public  Double MonthConsumption(Map<String, Object> params)
    {
        Calendar now =Calendar.getInstance();
        params.put("year",now.get(Calendar.YEAR));
        params.put("month",now.get(Calendar.MONTH)+1);
        Double sum=billCostMapper.MonthConsumption(params);
        return sum;
    }
    //今日消费
    public  Double DayConsumption(Map<String, Object> params)
    {
        Calendar now =Calendar.getInstance();
        params.put("year",now.get(Calendar.YEAR));
        params.put("month",now.get(Calendar.MONTH)+1);
        params.put("day",now.get(Calendar.DATE));
        Double sum=billCostMapper.MonthConsumption(params);
        return sum;
    }
    //搜索引擎完成率（管理员，渠道商，代理商）
    public  Double searchCompleteness(String search, LoginUser loginUser)
    {
        //今日达标数
        //1,先获取对应所有的订单
        Map<String,Object> billparam=new HashMap<>();
        if(loginUser.hasRole("ASSISTANT"))
        {
            billparam.put("userId",loginUser.getCreateUserId());

        }
        else
        {
            billparam.put("userId",loginUser.getId());
        }
        billparam.put("state",2);
        billparam.put("searchName",search);
        List<Bill> billList=billMapper.selectByInMemberId(billparam);
        //判断哪些订单今日达标
        int standardSum=0;//今天达标数
        if(billList.size()>0)
        {
            for (Bill bill:billList
                    ) {
                //对应订单排名标准
                BillPrice billPrice=new BillPrice();
                billPrice.setBillId(bill.getId());
                billPrice.setInMemberId(loginUser.getId());
                List<BillPrice> billPriceList=new ArrayList<>();
                billPriceList=billPriceMapper.selectByBillPrice(billPrice);
                //判断今日订单达到哪个标准
                for (BillPrice item:billPriceList
                        ) {
                    if(bill.getNewRanking()<=item.getBillRankingStandard())
                    {
                        standardSum+=1;
                        break;
                    }
                }
            }
        }
        double Completeness=0.0;
        if(billList.size()>0)
        {
            Completeness=((double)standardSum/billList.size())*100;
        }

        return Completeness;
    }
    //搜索引擎完成率（操作员）
    public  Double searchCompletenessBycmm(String search, LoginUser loginUser)
    {
        //今日达标数
        //1,先获取对应所有的订单
        Map<String,Object> billparam=new HashMap<>();
        billparam.put("userId",loginUser.getCreateUserId());
        billparam.put("state",2);
        billparam.put("billAscription",loginUser.getId());
        billparam.put("searchName",search);
        List<Bill> billList=billMapper.selectByInMemberId(billparam);
        //判断哪些订单今日达标
        int standardSum=0;//今天达标数
        if(billList.size()>0)
        {
            for (Bill bill:billList
                    ) {
                //对应订单排名标准
                BillPrice billPrice=new BillPrice();
                billPrice.setBillId(bill.getId());
                billPrice.setInMemberId(loginUser.getCreateUserId());
                List<BillPrice> billPriceList=new ArrayList<>();
                billPriceList=billPriceMapper.selectByBillPrice(billPrice);
                //判断今日订单达到哪个标准
                for (BillPrice item:billPriceList
                        ) {
                    if(bill.getNewRanking()<=item.getBillRankingStandard())
                    {
                        standardSum+=1;
                        break;
                    }
                }
            }
        }
        double Completeness=0.0;
        if(billList.size()>0)
        {
            Completeness=((double)standardSum/billList.size())*100;
        }

        return Completeness;
    }
    //搜索引擎完成率（客户）
    public  Double searchCompletenessBycus(String search, LoginUser loginUser)
    {
        //今日达标数
        //1,先获取对应所有的订单
        Map<String,Object> billparam=new HashMap<>();
        billparam.put("userId",loginUser.getId());
        billparam.put("state",2);
        billparam.put("searchName",search);
        List<Bill> billList=billMapper.getBillCountByCmm(billparam);
        //判断哪些订单今日达标
        int standardSum=0;//今天达标数
        if(billList.size()>0)
        {
            for (Bill bill:billList
                    ) {
                //对应订单排名标准
                BillPrice billPrice=new BillPrice();
                billPrice.setBillId(bill.getId());
                billPrice.setOutMemberId(loginUser.getId());
                List<BillPrice> billPriceList=new ArrayList<>();
                billPriceList=billPriceMapper.selectByBillPrice(billPrice);
                //判断今日订单达到哪个标准
                for (BillPrice item:billPriceList
                        ) {
                    if(bill.getNewRanking()<=item.getBillRankingStandard())
                    {
                        standardSum+=1;
                        break;
                    }
                }
            }
        }
        double Completeness=0.0;
        if(billList.size()>0)
        {
            Completeness=((double)standardSum/billList.size())*100;
        }

        return Completeness;
    }
    //获取一个月的天数
    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 某一个月第一天和最后一天
     * @param date
     * @return
     */
    private static Map<String, String> getFirstday_Lastday_Month(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        Date theDate = calendar.getTime();

        //上个月第一天
        GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
        gcLast.setTime(theDate);
        gcLast.set(Calendar.DAY_OF_MONTH, 1);
        String day_first = df.format(gcLast.getTime());
        StringBuffer str = new StringBuffer().append(day_first).append(" 00:00:00");
        day_first = str.toString();

        //上个月最后一天
        calendar.add(Calendar.MONTH, 1);    //加一个月
        calendar.set(Calendar.DATE, 1);        //设置为该月第一天
        calendar.add(Calendar.DATE, -1);    //再减一天即为上个月最后一天
        String day_last = df.format(calendar.getTime());
        StringBuffer endStr = new StringBuffer().append(day_last).append(" 23:59:59");
        day_last = endStr.toString();

        Map<String, String> map = new HashMap<String, String>();
        map.put("first", day_first);
        map.put("last", day_last);
        return map;
    }

    public String getYAxis(int max)
    {
        String yAxis="";
        if(max<=50)
        {
            yAxis="0,5,10,15,20,25,30,35,40,45";
        }
        else if(max<=100&&max>50)
        {

            yAxis="50,55,60,65,70,75,80,85,90,95";
        }
        else if(max>100&&max<=200)
        {

            yAxis="100,110,120,130,140,150,160,170,180,190";
        }
        else if(max<=300&&max>200)
        {
            yAxis="200,210,220,230,240,250,260,270,280,290";
        }
        else if(max<=400&&max>300)
        {
            yAxis="300,310,320,330,340,350,360,370,380,390";
        }
        else if(max<=500&&max>400)
        {
            yAxis="400,410,420,430,440,450,460,470,480,490";
        }

        else if(max<=5000&&max>500)
        {
            yAxis="500,1000,1500,2000,2500,3000,3500,4000,4500,5000";
        }

        else if(max<=10000&&max>5000)
        {
            yAxis="5500,6000,6500,7000,7500,8000,8500,9000,9500,10000";
        }
        else if(max<=15000&&max>10000)
        {
            yAxis="10500,11000,11500,12000,12500,13000,13500,14000,14500,15000";
        }

        else if(max<=20000&&max>15000)
        {
            yAxis="15500,16000,16500,17000,17500,18000,18500,19000,19500,20000";
        }
        else if(max<=30000&&max>20000)
        {
            yAxis="21000,22000,23000,24000,25000,26000,27000,28000,29000,30000";
        }



        return yAxis;
    }

    public String getYAxisSum(double max)
    {
        String yAxis="";
        if(max<=50)
        {
            yAxis="0,5,10,15,20,25,30,35,40,45";
        }
        else if(max<=100&&max>50)
        {

            yAxis="50,55,60,65,70,75,80,85,90,95";
        }
        else if(max>100&&max<=200)
        {

            yAxis="100,110,120,130,140,150,160,170,180,190";
        }
        else if(max<=300&&max>200)
        {
            yAxis="200,210,220,230,240,250,260,270,280,290";
        }
        else if(max<=400&&max>300)
        {
            yAxis="300,310,320,330,340,350,360,370,380,390";
        }
        else if(max<=500&&max>400)
        {
            yAxis="400,410,420,430,440,450,460,470,480,490";
        }

        else if(max<=5000&&max>500)
        {
            yAxis="500,1000,1500,2000,2500,3000,3500,4000,4500,5000";
        }

        else if(max<=10000&&max>5000)
        {
            yAxis="5500,6000,6500,7000,7500,8000,8500,9000,9500,10000";
        }


        else if(max<=15000&&max>10000)
        {
            yAxis="10500,11000,11500,12000,12500,13000,13500,14000,14500,15000";
        }

        else if(max<=20000&&max>15000)
        {
            yAxis="15500,16000,16500,17000,17500,18000,18500,19000,19500,20000";
        }
        else if(max<=30000&&max>20000)
        {
            yAxis="21000,22000,23000,24000,25000,26000,27000,28000,29000,30000";
        }



        return yAxis;
    }
    @Override
    public List<noticepublish> getInBox(LoginUser loginUser) {
        Map<String,Object> params=new HashMap<>();
        params.put("currentid", loginUser.getId().toString());
        params.put("type", 1);
        params.put("offset", 0);
        params.put("limit", 3);
        List<noticepublish> inBoxList=noticepublishMapper.selectByInUser(params);
        return inBoxList;
    }

}
