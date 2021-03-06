/*
 * Copyright (c) 2016 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.terasoluna.fw.batch.executor.repository;

import static java.util.Arrays.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static uk.org.lidalia.slf4jtest.LoggingEvent.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jp.terasoluna.fw.batch.constants.JobStatusConstants;
import jp.terasoluna.fw.batch.executor.dao.SystemDao;
import jp.terasoluna.fw.batch.executor.vo.BLogicResult;
import jp.terasoluna.fw.batch.executor.vo.BatchJobData;
import jp.terasoluna.fw.batch.executor.vo.BatchJobManagementParam;
import jp.terasoluna.fw.batch.executor.vo.BatchJobManagementUpdateParam;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/**
 * JobStatusChangerImplのテストケースクラス
 */
public class JobStatusChangerImplTest {

    private SystemDao mockSystemDao = mock(SystemDao.class);

    private PlatformTransactionManager mockPlatformTransactionManager = mock(
            PlatformTransactionManager.class);

    private JobStatusChanger jobStatusChanger = new JobStatusChangerImpl(mockSystemDao, mockPlatformTransactionManager);

    private TestLogger logger = TestLoggerFactory.getTestLogger(
            JobStatusChangerImpl.class);

    @Before
    public void setUp() {
        // テスト入力データ設定
        Mockito.reset(mockSystemDao, mockPlatformTransactionManager);
    }

    /**
     * テスト後処理：ロガーのクリアを行う。
     */
    @After
    public void tearDown() {
        logger.clear();
    }

    /**
     * コンストラクタテスト 【正常系】
     * 
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・パラメータとテスト対象のフィールド変数が一致するかどうか
     * </pre>
     */
    @Test
    public void testJobStatusChangerImpl01() {

        // テスト実施
        JobStatusChangerImpl target = new JobStatusChangerImpl(mockSystemDao, mockPlatformTransactionManager);

        // 結果検証
        assertSame(mockSystemDao, target.systemDao);
        assertSame(mockPlatformTransactionManager,
                target.adminTransactionManager);
    }

    /**
     * コンストラクタテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・systemDaoにNullを指定する
     * 確認項目
     * ・IllegalArgumentExceptionが発生すること
     * </pre>
     */
    @Test
    public void testJobStatusChangerImpl02() {

        // テスト実施
        try {
            new JobStatusChangerImpl(null, mockPlatformTransactionManager);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "[EAL025056] [Assertion failed] - JobStatusChangerImpl requires to set systemDao. please confirm the settings.",
                    e.getMessage());
        }
    }

    /**
     * コンストラクタテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・platformTransactionManagerにNullを指定する
     * 確認項目
     * ・IllegalArgumentExceptionが発生すること
     * </pre>
     */
    @Test
    public void testJobStatusChangerImpl03() {

        // テスト実施
        try {
            new JobStatusChangerImpl(mockSystemDao, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "[EAL025056] [Assertion failed] - JobStatusChangerImpl requires to set adminTransactionManager. please confirm the settings.",
                    e.getMessage());
        }
    }

    /**
     * changeToStartStatusテスト 【正常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * 確認項目
     * ・trueが返却されること
     * [DAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されること
     * ・PlatformTransactionManager#rollback()が呼び出されないこと
     * </pre>
     */
    @Test
    public void testChangeToStartStatus01() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_UNEXECUTION);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenReturn(1);
        when(mockTran.isCompleted()).thenReturn(true);

        // テスト実行
        // 結果検証
        assertTrue(jobStatusChanger.changeToStartStatus("00000001"));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:1"))));
        verify(mockPlatformTransactionManager).commit(mockTran);
        verify(mockPlatformTransactionManager, never()).rollback(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・無効(データなし)のジョブシーケンスIDが渡されること
     * 確認項目
     * ・falseが返却されること
     * ・[IAL025024]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus02() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(null);

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025024] This job has already been started by another. jobSequenceId:00000001"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・ジョブシーケンスIDとしてnullが渡されること
     * 確認項目
     * ・falseが返却されること
     * ・[IAL025024]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus03() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(null);

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToStartStatus(null));
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025024] This job has already been started by another. jobSequenceId:null"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:null"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONではないこと
     * 確認項目
     * ・falseが返却されること
     * ・[DAL025055]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus04() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(JobStatusConstants.JOB_STATUS_FAILURE);
                    }
                });

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025055] This job status at the job control table is already updated by another worker. It will be skip. jobSequenceId:00000001 expectedCurAppStatus:0 actualCurAppStatus:3 changeTo:1"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * ・ジョブステータスの更新に失敗すること
     * 確認項目
     * ・falseが返却されること
     * ・[DAL025023]、[EAL025025]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus05() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("0000000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_UNEXECUTION);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenReturn(0);

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:1"),
                error("[EAL025025] Job status update error. jobSequenceId:00000001 blogicStatus:null"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * ・TransactionStatusを取得する際に例外が発生すること
     * 確認項目
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されないこと
     * </pre>
     */
    @Test
    public void testChangeToStartStatus06() {
        // テスト入力データ設定
        Exception ex = new RuntimeException("test");
        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenThrow(ex);

        // テスト実行
        // 結果検証
        try {
            jobStatusChanger.changeToStartStatus("00000001");
            fail();
        } catch (Exception e) {
            assertSame(ex, e);
            verify(mockPlatformTransactionManager, never()).commit(any(
                    TransactionStatus.class));
            verify(mockPlatformTransactionManager, never()).rollback(any(
                    TransactionStatus.class));
        }

    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * 確認項目
     * ・systemDao#selectJob()で例外がスローされること
     * ・[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus07() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException re = new RuntimeException("Test exception.");

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenThrow(re);

        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
            fail();
        } catch (Exception e) {
            assertSame(re, e);
        }

        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * 確認項目
     * ・systemDao#updateJob()で例外がスローされること
     * ・[DAL025023]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus08() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException re = new RuntimeException("Test exception.");

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_UNEXECUTION);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenThrow(re);

        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
            fail();
        } catch (Exception e) {
            assertSame(re, e);
        }
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:1"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToStartStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_UNEXECUTIONであること
     * 確認項目
     * ・systemDao#updateJob()で例外がスローされること
     * ・さらに、ロールバックで例外がスローされること
     * ・[DAL025023]、[IAL025023]、[EAL025064]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToStartStatus09() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException re = new RuntimeException("Test exception.");
        RuntimeException reAtRollback = new RuntimeException("Test exception at Rollback.");
    
        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_UNEXECUTION);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenThrow(re);
        doThrow(reAtRollback).when(mockPlatformTransactionManager).rollback(any(
                TransactionStatus.class));

        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToStartStatus("00000001"));
            fail();
        } catch (Exception e) {
            assertSame(re, e);
        }
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:1"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"),
                error(reAtRollback, "[EAL025064] Failed to rollback transaction. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【正常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_EXECUTINGであること
     * 確認項目
     * ・trueが返却されること
     * [DAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus01() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_EXECUTING);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenReturn(1);
        when(mockTran.isCompleted()).thenReturn(true);

        // テスト実行
        // 結果検証
        assertTrue(jobStatusChanger.changeToEndStatus("00000001",
                blogicResult));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:2"))));
        verify(mockPlatformTransactionManager).commit(mockTran);
        verify(mockPlatformTransactionManager, never()).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・無効(データなし)のジョブシーケンスIDが渡されること
     * 確認項目
     * ・falseが返却されること
     * ・[IAL025024]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus02() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(null);

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                blogicResult));
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025024] This job has already been started by another. jobSequenceId:00000001"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・ジョブシーケンスIDとしてnullが渡されること
     * 確認項目
     * ・falseが返却されること
     * ・[IAL025024]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus03() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);

        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(null);

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToEndStatus(null, blogicResult));
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025024] This job has already been started by another. jobSequenceId:null"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:null"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・blogicResultとしてnullが渡されること
     * 確認項目
     * ・NullPointerExceptionがスローされること
     * ・[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus04() {
        // テスト入力データ設定
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_EXECUTING);
                    }
                });

        try {
            // テスト実行
            jobStatusChanger.changeToEndStatus("00000001", null);
            fail();
        } catch (Exception e) {
            // 結果検証
            assertTrue(e instanceof NullPointerException);
        }
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_EXECUTINGではないこと
     * 確認項目
     * ・falseが返却されること
     * ・[DAL025055]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus05() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);

        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_PROCESSED);
                    }
                });

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                blogicResult));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025055] This job status at the job control table is already updated by another worker. It will be skip. jobSequenceId:00000001 expectedCurAppStatus:1 actualCurAppStatus:2 changeTo:2"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブステータスの更新に失敗すること
     * 確認項目
     * ・falseが返却されること
     * ・[DAL025023]、[EAL025025]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus06() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);

        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("00000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_EXECUTING);
                    }
                });

        // テスト実行
        // 結果検証
        assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                blogicResult));
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:2"),
                error("[EAL025025] Job status update error. jobSequenceId:00000001 blogicStatus:255"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・TransactionStatusを取得する際に例外が発生すること
     * 確認項目
     * ・trueが返却されること
     * [DAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されないこと
     * </pre>
     */
    @Test
    public void testChangeToEndStatus07() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        Exception ex = new RuntimeException("test");

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenThrow(ex);

        // テスト実行
        // 結果検証
        try {
            jobStatusChanger.changeToEndStatus("00000001", blogicResult);
            fail();
        } catch (Exception e) {
            assertSame(ex, e);
            verify(mockPlatformTransactionManager, never()).commit(any(
                    TransactionStatus.class));
            verify(mockPlatformTransactionManager, never()).rollback(any(
                    TransactionStatus.class));
        }

    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_EXECUTINGであること
     * 確認項目
     * ・systemDao#selectJob()で例外がスローされること
     * ・[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus08() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException re = new RuntimeException("Test exception.");

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenThrow(re);

        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                    blogicResult));
            fail();
        } catch (Exception e) {
            assertSame(re, e);
        }
        assertThat(logger.getLoggingEvents(), is(asList(info(
                "[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_EXECUTINGであること
     * 確認項目
     * ・systemDao#updateJob()で例外がスローされること
     * ・[DAL025023]、[IAL025023]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus09() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException runtimeException = new RuntimeException("Test exception.");

        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("0000000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_EXECUTING);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenThrow(
                        runtimeException);

        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                    blogicResult));
            fail();
        } catch (Exception e) {
            assertSame(runtimeException, e);
        }
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:2"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

    /**
     * changeToEndStatusテスト 【異常系】
     * 
     * <pre>
     * 事前条件
     * ・有効なジョブシーケンスIDが渡されること
     * ・ジョブのステータスがJOB_STATUS_EXECUTINGであること
     * 確認項目
     * ・systemDao#updateJob()で例外がスローされること
     * ・さらに、ロールバックで例外がスローされること
     * ・[DAL025023]、[IAL025023]、[EAL025064]のログが出力されること
     * ・PlatformTransactionManager#commit()が呼び出されないこと
     * ・PlatformTransactionManager#rollback()が呼び出されること
     * </pre>
     */
    @Test
    public void testChangeToEndStatus10() {
        // テスト入力データ設定
        BLogicResult blogicResult = new BLogicResult();
        TransactionStatus mockTran = mock(TransactionStatus.class);
        RuntimeException re = new RuntimeException("Test exception.");
        RuntimeException reAtRollback = new RuntimeException("Test exception at Rollback.");
    
        when(mockPlatformTransactionManager.getTransaction(any(
                DefaultTransactionDefinition.class))).thenReturn(mockTran);
        when(mockSystemDao.selectJob(any(BatchJobManagementParam.class)))
                .thenReturn(new BatchJobData() {
                    {
                        setJobSequenceId("0000000001");
                        setCurAppStatus(
                                JobStatusConstants.JOB_STATUS_EXECUTING);
                    }
                });
        when(mockSystemDao.updateJobTable(any(
                BatchJobManagementUpdateParam.class))).thenThrow(re);
        doThrow(reAtRollback).when(mockPlatformTransactionManager).rollback(any(
                TransactionStatus.class));
    
        // テスト実行
        // 結果検証
        try {
            assertFalse(jobStatusChanger.changeToEndStatus("00000001",
                    blogicResult));
            fail();
        } catch (Exception e) {
            assertSame(re, e);
        }
        assertThat(logger.getLoggingEvents(), is(asList(debug(
                "[DAL025023] Try to update status jobSequenceId:00000001 changeStatus:2"),
                info("[IAL025023] Skipped processing of updating the job status. This transaction will be attempt to roll-back. jobSequenceId:00000001"),
                error(reAtRollback, "[EAL025064] Failed to rollback transaction. jobSequenceId:00000001"))));
        verify(mockPlatformTransactionManager, never()).commit(mockTran);
        verify(mockPlatformTransactionManager).rollback(mockTran);
    }

}
