/*
 * Copyright (c) 2007 NTT DATA Corporation
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

package jp.terasoluna.fw.validation.springmodules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.Form;
import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.ValidatorResult;
import org.apache.commons.validator.ValidatorResults;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link jp.terasoluna.fw.validation.springmodules.CommonsValidatorEx}
 * クラスのテスト。
 * 
 * <p>
 * <h4>【クラスの概要】</h4>
 * Jakaruta-CommonsのValidator拡張クラス。
 * <p>
 * 
 * @see jp.terasoluna.fw.validation.springmodules.CommonsValidatorEx
 */
public class CommonsValidatorExTest {

    /**
     * testGetValidatorException01() <br>
     * <br>
     * 
     * (正常系) <br>
     * 観点：A,C <br>
     * <br>
     * 入力値：(前提条件) this.validatorException:ValidatorException<br>
     * 
     * <br>
     * 期待値：(戻り値)
     * ValidatorException:this.validatorExceptionと同一インスタンスのValidatorException<br>
     * 
     * <br>
     * 属性に設定されているvalidatorExceptionを返却することのテスト。 <br>
     * 
     * @throws Exception
     *             このメソッドで発生した例外
     */
    @Test
    public void testGetValidatorException01() throws Exception {
        // 前処理
        ValidatorResources resources = new ValidatorResources();
        CommonsValidatorEx commonsValidatorEx = new CommonsValidatorEx(
                resources, null);
        ValidatorException validatorException = new ValidatorException();
        ReflectionTestUtils.setField(commonsValidatorEx, "validatorException",
                validatorException);

        // テスト実施
        ValidatorException resultValidatorException = commonsValidatorEx
                .getValidatorException();

        // 判定
        assertSame(validatorException, resultValidatorException);
    }

    /**
     * testValidate01() <br>
     * <br>
     * 
     * (正常系) <br>
     * 観点：A <br>
     * <br>
     * 入力値：(前提条件) super.validate():例外をスローしない<br>
     * 
     * <br>
     * 期待値：(戻り値) ValidatorResults:super.validate()の結果<br>
     * (状態変化) this.validatorException:null<br>
     * 
     * <br>
     * super.validate()が例外をスローしない場合、super.validate()の結果を返却することのテスト。。
     * <br>
     * 
     * @throws Exception
     *             このメソッドで発生した例外
     */
    @Test
    public void testValidate01() throws Exception {
        // 前処理
        CommonsValidatorEx_ValidatorResourcesStub01 resources = 
            new CommonsValidatorEx_ValidatorResourcesStub01();
        Form form = new Form();
        resources.setForm(form);

        // super.validate()のオーバーライドはできないため、
        // Fieldのモッククラスを作成して、
        // super.validate()が呼び出す、field.validate()の
        // 結果を操作する。
        // super.validate()は、field.validate()の結果をマージして返却している。
        CommonsValidatorEx_FieldStub01 field =
            new CommonsValidatorEx_FieldStub01();
        List<Field> lFields = new ArrayList<Field>();
        lFields.add(field);
        ReflectionTestUtils.setField(form, "lFields", lFields);

        ValidatorResults validatorResults = new ValidatorResults();
        Map<String, ValidatorResult> hResults
            = new HashMap<String, ValidatorResult>();
        ValidatorResult validatorResult = new ValidatorResult(field);
        hResults.put("test", validatorResult);
        ReflectionTestUtils.setField(validatorResults, "hResults", hResults);
        
        field.validateReturn = validatorResults;
        
        CommonsValidatorEx commonsValidatorEx = new CommonsValidatorEx(
                resources, "formName");

        // テスト実施
        ValidatorResults result = commonsValidatorEx.validate();

        // 判定
        // resultが、field.validate()の結果を含んでいるかを確認する。
        Map<?, ?> resultHResults = (Map<?, ?>) ReflectionTestUtils.getField(result, "hResults");
        assertEquals(1, resultHResults.size());
        assertSame(validatorResult, resultHResults.get("test"));
    }

    /**
     * testValidate02() <br>
     * <br>
     * 
     * (正常系) <br>
     * 観点：A,G <br>
     * <br>
     * 入力値：(前提条件) super.validate():ValidatorExceptionをスローする<br>
     * 
     * <br>
     * 期待値：(状態変化) 例外:super.validate()がスローしたValidatorException<br>
     * (状態変化) this.validatorException:super.validate()がスローしたValidatorException<br>
     * 
     * <br>
     * super.validate()がValidatorExceptionをスローする場合、その例外を属性に設定した後、そのままスローすることのテスト。
     * <br>
     * 
     * @throws Exception
     *             このメソッドで発生した例外
     */
    @Test
    public void testValidate02() throws Exception {
        // 前処理
        CommonsValidatorEx_ValidatorResourcesStub01 resources = 
            new CommonsValidatorEx_ValidatorResourcesStub01();
        Form form = new Form();
        resources.setForm(form);

        // super.validate()のオーバーライドはできないため、
        // Fieldのモッククラスを作成して、
        // super.validate()が呼び出す、field.validate()の
        // 結果を操作する。
        // super.validate()は、field.validate()がスローしたvalidatorExceptionを
        // そのままスローしている。
        CommonsValidatorEx_FieldStub01 field =
            new CommonsValidatorEx_FieldStub01();
        List<Field> lFields = new ArrayList<Field>();
        lFields.add(field);
        ReflectionTestUtils.setField(form, "lFields", lFields);

        field.validatorException = new ValidatorException();
        
        CommonsValidatorEx commonsValidatorEx = new CommonsValidatorEx(
                resources, "formName");

        // テスト実施
        try {
            commonsValidatorEx.validate();
            fail();
        } catch (ValidatorException e) {
            // 判定
            // field.validate()がスローしたValidatorExceptionと同一インスタンスかを確認する。
            assertSame(field.validatorException, e);
            assertSame(e, commonsValidatorEx.getValidatorException());
        }
    }
    
    /**
     * testClear01()
     * <br><br>
     * 
     * (正常系)
     * <br>
     * 観点：A,C
     * <br><br>
     * 入力値：(前提条件) this.validatorException:ValidatorException<br>
     *         
     * <br>
     * 期待値：(状態変化) this.validatorException:null<br>
     *         
     * <br>
     * 属性に設定されているvalidatorExceptionをクリアすることのテスト。
     * <br>
     * 
     * @throws Exception このメソッドで発生した例外
     */
    @Test
    public void testClear01() throws Exception {
        // 前処理
        ValidatorResources resources = new ValidatorResources();
        CommonsValidatorEx commonsValidatorEx = new CommonsValidatorEx(
                resources, null);
        ValidatorException validatorException = new ValidatorException();
        ReflectionTestUtils.setField(commonsValidatorEx, "validatorException",
                validatorException);

        // テスト実施
        commonsValidatorEx.clear();
        ValidatorException result = (ValidatorException) ReflectionTestUtils
            .getField(commonsValidatorEx, "validatorException");

        // 判定
        assertNull(result);
    }
}
