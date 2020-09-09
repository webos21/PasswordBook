package com.gmail.webos21.passwordbook.db;

import java.util.List;

public interface PbDbInterface {

    List<PbRow> findRows();

    List<PbRow> findRows(String keyString);

    PbRow getRow(Long id);

    PbRow getRow(PbRow aRow);

    boolean updateRow(PbRow newRow);

    int deleteRow(Long id);

    int deleteRow(PbRow aRow);

}
