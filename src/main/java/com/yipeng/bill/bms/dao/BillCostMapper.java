package com.yipeng.bill.bms.dao;

import com.yipeng.bill.bms.domain.BillCost;

import java.util.List;
import java.util.Map;

public interface BillCostMapper {
    int deleteByPrimaryKey(Long id);
    int deleteByBillId(Long id);
    int insert(BillCost record);

    int insertSelective(BillCost record);

    BillCost selectByPrimaryKey(Long id);

    List<BillCost> selectByBillId(Long BillId);

    BillCost selectByBillIdAndDate(Map<String, Object> modelMap);

    int updateByPrimaryKeySelective(BillCost record);

    int updateByPrimaryKey(BillCost record);

    Double selectByPriceSum(Map<String, Object> modelMap);

    int selectByPriceCount(Map<String, Object> modelMap);

    List<BillCost> getPriceByMap(Map<String, Object> params);

    Long getPriceByMapCount(Map<String, Object> params);

    /**
     * @param params
     * @return
     */
    Double MonthConsumption(Map<String, Object> params);
    Double MonthConsumptionCommissioner(Map<String, Object> params);
    Double MonthConsumptionCustomer(Map<String, Object> params);

    int  selectByBillCostOfDay(Map<String, Object> params);
    Double selectByBillCostOfWeek(Map<String, Object> params);
    Double selectByBillCostOfMonthNow(Map<String, Object> params);
    Double selectByBillCostOfMonth(Map<String, Object> params);
    Double selectByBillCostOfAll(Map<String, Object> params);

    Double  selectByBillCostOfDaySum(Map<String, Object> params);

    /**
     * 扣费  判断当天订单是否消费
     * @param params
     * @return
     */
    List<BillCost> selectByDayCost(Map<String, Object> params);

    /**
     * 扣费  通过单价Id判断今日是否存在
     * @param params
     * @return
     */
       BillCost selectByDayCostPriceId(Map<String, Object> params);
    /**
     * 获取上一次的扣费记录
     * @param params
     * @return
     */
        BillCost selectByCostByOutId(Map<String, Object> params);

    /**
     * 判断今日消费是否存在
     * @param params
     * @return
     */
        BillCost selectByDayCostExisit(Map<String, Object> params);

    /**
     * 获取每个付款人的消费
     * @param params
     * @return
     */
    Double selectByUseDayCost(Map<String, Object> params);

    /**
     * 优化方结算（本日）
     * @param params
     * @return
     */
    Double selectByBillDayCost(Map<String, Object> params);

    /**
     * 优化方结算（本月）
     * @param params
     * @return
     */
    Double selectByBillMonthCost(Map<String, Object> params);

    /**
     * 客户方结算（本日）
     * @param params
     * @return
     */
    Double selectByBillClientDayCost(Map<String, Object> params);

    /**
     * 客户方结算（本月）
     * @param params
     * @return
     */
    Double selectByBillClientMonthCost(Map<String, Object> params);

    /**
     * 操作员业绩（本日）
     * @param params
     * @return
     */
    Double selectByBillCaoZuoYuanDayCost(Map<String, Object> params);

    /**
     * 操作员业绩（本月）
     * @param params
     * @return
     */
    Double selectByBillCaoZuoYuanMonthCost(Map<String, Object> params);

    List<BillCost>  selectByGetBillToOne();
    List<BillCost> selectByCaoZuoyuanGetBillToOne(Map<String, Object> params);
    List<BillCost> selectByQuDaoGetBillToOne(Map<String, Object> params);

    List<Map<String,Object>> selectByBillCostOfDaySumGroupBy(Map<String, Object> params);
    List<Map<String,Object>> selectByBillCostOfDayGroupBy(Map<String, Object> params);
}