package com.pinyougou.solr.utils;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtils {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    private void ImportItemData() {
        // 查询数据库
        Item item = new Item();
        item.setStatus("1");
        List<Item> itemList = itemMapper.select(item);
        System.out.println(itemList.size());

        List<SolrItem> solrItemList = new ArrayList<>();
        for (Item item1 : itemList) {
            SolrItem solrItem = new SolrItem();
            solrItem.setId(item1.getId());
            solrItem.setTitle(item1.getTitle());
            solrItem.setPrice(item1.getPrice());
            solrItem.setImage(item1.getImage());
            solrItem.setGoodsId(item1.getGoodsId());
            solrItem.setCategory(item1.getCategory());
            solrItem.setBrand(item1.getBrand());
            solrItem.setSeller(item1.getSeller());
            solrItem.setUpdateTime(item1.getUpdateTime());
            solrItem.setItemSaleCount(item1.getNum());

            // 将规格spec的json字符串转换为map
            Map specMap = JSON.parseObject(item1.getSpec(), Map.class);
            solrItem.setSpecMap(specMap);

            solrItemList.add(solrItem);
        }

        // 保存到索引库
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItemList);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        SolrUtils solrUtils = applicationContext.getBean(SolrUtils.class);
        solrUtils.ImportItemData();
    }
}
