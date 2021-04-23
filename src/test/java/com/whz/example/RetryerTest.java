package com.whz.example;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wanghz
 * @Date: 2020/8/12 5:41 PM
 */
public class RetryerTest {


    /**
     * 发送邮件失败进行重试的案例：有些邮件服务器会进行发送频率的限制，从而导致有时邮件会发送失败，这里以定时重试的方式进行补偿优化
     */
    @Test
    public void test() {
        send("test", "test", Lists.newArrayList("test@qq.com)"), null);
    }

    /**
     * 发送Email
     *
     * @param subject   邮件主题
     * @param content   邮件内容，可编写html代码
     * @param to        邮件接收者用户名
     * @param fileUrls  邮件附件（相对地址）
     * @return
     * @throws IOException
     */
    public void send(String subject, String content, List<String> to, List<String> fileUrls) {

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                // 如果邮件发送失败进行重试
                .retryIfResult(Predicates.equalTo(false))
                // 重试3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 每次按5秒递增的时间间隔进行重试
                .withWaitStrategy(
                        WaitStrategies.incrementingWait(0, TimeUnit.SECONDS, 5, TimeUnit.SECONDS)
                )
                .build();

        try {
            retryer.call(createTask(subject, content, to, fileUrls));
        } catch (ExecutionException e) {
            // log.error("邮件发送执行异常", e);
        } catch (RetryException e) {
            // log.error("邮件发送重试异常", e);
        }

    }

    /**
     * 创建发送邮件的任务
     *
     * @param subject
     * @param content
     * @param to
     * @param fileUrls
     * @return
     */
    private Callable createTask(String subject, String content, List<String> to, List<String> fileUrls) {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // MailSendRequestV1 request = new MailSendRequestV1();
                // request.setSceneId(sceneId);
                // request.setLang(I18n2LangEnum.SIMPLIFIED_CHINESE);
                // request.setSendToList(to);
                // request.setBizType(0);
                // request.setSendTypeEnum(MsgSendTypeEnum.BOSS_PLATFORM);
                // request.setSenderName(SENDER_NAME);
                //
                // request.setTitle(subject);
                // request.setContent(content);
                // request.setAttachmentPath(fileUrls);
                //
                // MsgResult<Boolean> result = Gotone.mail.sendSync(request);
                // if (!result.isSuccess()) {
                //     log.error("邮件发送失败：request = {}, result = {}", JSONObject.toJSONString(request), JSONObject.toJSONString(result));
                //     return false;
                // }
                //
                // return result.isSuccess();

                System.out.println("模拟发送邮件。。。");
                return false;
            }
        };

    }

    static int count = 0;

    public static void main(String[] args) {
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                // 如果任务失败则重试
                .retryIfResult(Predicates.equalTo(false))
                // 重试5次（包括第一次调用）
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                // 每次按1秒递增的时间间隔进行重试
                .withWaitStrategy(
                        // 任务第一次调用后会sleep 2 秒，后面每次sleep 1秒，2秒，3秒依次类推
                        WaitStrategies.incrementingWait(2, TimeUnit.SECONDS, 1, TimeUnit.SECONDS)
                ).build();

        try {
            retryer.call(createAction());
        } catch (ExecutionException e) {
            // 任务制定异常就会走这里
            System.out.println("ExecutionException");
        } catch (RetryException e) {
            // 重试到最后一次都没成功会走这里
            System.out.println("RetryException");
        }
    }

    private static Callable createAction() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                count++;
                System.out.println(count + "执行任务:" + System.currentTimeMillis());
                if (count == 7) {
                    throw new RuntimeException();
                }
                return false;
            }
        };
    }

}
