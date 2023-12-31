package com.yidiansishiyi.gataoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yidiansishiyi.gataoj.annotation.AuthCheck;
import com.yidiansishiyi.gataoj.common.BaseResponse;
import com.yidiansishiyi.gataoj.common.DeleteRequest;
import com.yidiansishiyi.gataoj.common.ErrorCode;
import com.yidiansishiyi.gataoj.common.ResultUtils;
import com.yidiansishiyi.gataoj.constant.UserConstant;
import com.yidiansishiyi.gataoj.exception.BusinessException;
import com.yidiansishiyi.gataoj.exception.ThrowUtils;
import com.yidiansishiyi.gataoj.model.dto.questioncomment.QuestionCommentAddRequest;
import com.yidiansishiyi.gataoj.model.dto.questioncomment.QuestionCommentQueryRequest;
import com.yidiansishiyi.gataoj.model.dto.questioncomment.QuestionCommentUpdateRequest;
import com.yidiansishiyi.gataoj.model.entity.QuestionComment;
import com.yidiansishiyi.gataoj.model.entity.User;
import com.yidiansishiyi.gataoj.model.vo.CommentVO;
import com.yidiansishiyi.gataoj.service.QuestionCommentService;
import com.yidiansishiyi.gataoj.service.UserService;
import com.yidiansishiyi.gataoj.utils.SensitiveWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 题目评论接口
 *
 */
@RestController
@RequestMapping("/question_comment")
@Slf4j
public class QuestionCommentController {

    @Resource
    private QuestionCommentService questionCommentService;

    @Resource
    private UserService userService;

    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody QuestionCommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 敏感词校验
        Map<String, Integer> map = SensitiveWordUtil.matchWords(commentAddRequest.getContent());
        ThrowUtils.throwIf(map.size() > 0, ErrorCode.PARAMS_ERROR, "评论存在敏感词");
        Long commenId = null;
        try {
            commenId = questionCommentService.savaComment(commentAddRequest, request);
        } catch (Exception e) {
            log.error("评论失败{}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ResultUtils.success(commenId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionComment oldComment = questionCommentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionCommentService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param commentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateComment(@RequestBody QuestionCommentUpdateRequest commentUpdateRequest) {
        if (commentUpdateRequest == null || commentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionComment comment = new QuestionComment();
        BeanUtils.copyProperties(commentUpdateRequest, comment);
        // 添加校验
        long id = commentUpdateRequest.getId();
        // 判断是否存在
        QuestionComment oldComment = questionCommentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionCommentService.updateById(comment);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody QuestionCommentQueryRequest commentQueryRequest,
                                                             HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<CommentVO> voPage = questionCommentService.getCommentVO(commentQueryRequest);
        return ResultUtils.success(voPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<CommentVO>> listMyCommentVOByPage(@RequestBody QuestionCommentQueryRequest commentQueryRequest,
                                                               HttpServletRequest request) {
        if (commentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        commentQueryRequest.setUserId(loginUser.getId());
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionComment> commentPage = questionCommentService.page(new Page<>(current, size),
                questionCommentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(questionCommentService.getCommentVOPage(commentPage));
    }
}
