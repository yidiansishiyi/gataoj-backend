package com.yidiansishiyi.gataoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yidiansishiyi.gataoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.yidiansishiyi.gataoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.yidiansishiyi.gataoj.model.entity.QuestionSubmit;
import com.yidiansishiyi.gataoj.model.entity.User;
import com.yidiansishiyi.gataoj.model.vo.QuestionSubmitVO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.scheduling.annotation.Async;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 题目提交服务
 *
 * @author  sanqi
 *  
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest 题目提交信息
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO (QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    @Async
    boolean saveBatchAsync(LinkedList entityList, int batchSize);
}
