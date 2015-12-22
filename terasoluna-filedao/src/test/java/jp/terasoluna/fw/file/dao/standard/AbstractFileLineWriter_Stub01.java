package jp.terasoluna.fw.file.dao.standard;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

/**
 * AbstractFileLineWriterの試験で利用するファイル行オブジェクトのスタブクラス。<br>
 * <br>
 * 以下の@FileFormatの設定を持つ<br>
 * <ul>
 * <li>delimiter："|"(デフォルト値以外)</li>
 * <li>encloseChar："\""(デフォルト値以外)</li>
 * <li>lineFeedChar："\r"(デフォルト値以外)</li>
 * <li>fileEncoding："UTF-8"(デフォルト値以外)</li>
 * <li>overWriteFlg：true(デフォルト値以外)</li>
 * </ul>
 * <br>
 * フィールドは持たない<br>
 */
@FileFormat(delimiter = '|', encloseChar = '\"', lineFeedChar = "\r", fileEncoding = "UTF-8", overWriteFlg = true)
public class AbstractFileLineWriter_Stub01 {
    @OutputFileColumn(columnIndex = 0)
    private String dummy;

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
}
