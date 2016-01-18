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

package jp.terasoluna.fw.file.dao.standard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * パースを行わなないカラムパーサークラス。
 * <p>
 * 変換結果をファイル行オブジェクトのString型の属性に値を格納する。
 * </p>
 * <p>
 * フォーマット文字列は、無視される。
 * </p>
 */
public class NullColumnParser implements ColumnParser {

    /**
     * 指定された文字列をそのままファイル行オブジェクトに格納する。
     * @param column カラムの文字列
     * @param t ファイル行オブジェクト
     * @param method カラムの文字列をファイル行オブジェクトに格納するメソッド
     * @param columnFormat パースする際のフォーマット文字列
     * @throws IllegalArgumentException ファイル行オブジェクトのsetterメソッドのアクセスに失敗したとき
     * @throws IllegalAccessException ファイル行オブジェクトへの設定が失敗したとき
     * @throws InvocationTargetException ファイル行オブジェクトのメソッドが例外をスローしたとき
     */
    @Override
    public void parse(String column, Object t, Method method,
            String columnFormat) throws IllegalArgumentException,
                                IllegalAccessException,
                                InvocationTargetException {
        method.invoke(t, column);
    }
}
