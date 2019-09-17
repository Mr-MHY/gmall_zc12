package com.gamll.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.BaseAttrInfo;
import com.gmall.bean.BaseAttrValue;
import com.gmall.bean.SkuLsParams;
import com.gmall.bean.SkuLsResult;
import com.gmall.service.ListService;
import com.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    ManageService manageService;
    @Reference
    ListService listService;

    @GetMapping("list.html")
    public String list(SkuLsParams skuLsParams, Model model) {

        skuLsParams.setPageSize(2);
        SkuLsResult skuLsResult = listService.getSkuLsInfoList(skuLsParams);
        model.addAttribute("skuLsResult", skuLsResult);

        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList", attrList);

        String paramUrl = makeParamUrl(skuLsParams);

        List<BaseAttrValue> selectedValueList = new ArrayList<>();

        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                        String selectedValueId = skuLsParams.getValueId()[i];
                        if (baseAttrValue.getId().equals(selectedValueId)) {
                            iterator.remove();
                            String selectedParamUrl = makeParamUrl(skuLsParams, selectedValueId);
                            baseAttrValue.setParamUrl(selectedParamUrl);
                            selectedValueList.add(baseAttrValue);

                        }
                    }
                }
            }
        }
        model.addAttribute("paramUrl", paramUrl);
        model.addAttribute("selectedValueList", selectedValueList);
        model.addAttribute("keyword", skuLsParams.getKeyword());
        model.addAttribute("pageNo", skuLsParams.getPageNo());
        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        return "list";
    }

    private String makeParamUrl(SkuLsParams skuLsParams, String... excludeValueId) {
        String paramUrl="";
        if (skuLsParams.getKeyword()!=null){
            paramUrl+="keyword="+skuLsParams.getKeyword();
        }else if (skuLsParams.getCatalog3Id()!=null){
            paramUrl+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (int i = 0; i <skuLsParams.getValueId().length ; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (excludeValueId!=null&&excludeValueId.length>0){
                    String exValueId = excludeValueId[0];
                    if (valueId.equals(exValueId)){
                        continue;
                    }
                }
                if (paramUrl.length()>0){
                    paramUrl+="&";
                }
                paramUrl+="valueId="+valueId;
            }
        }

        return paramUrl;
    }
}


