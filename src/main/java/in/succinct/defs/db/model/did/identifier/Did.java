package in.succinct.defs.db.model.did.identifier;

import com.venky.swf.db.annotations.column.COLUMN_SIZE;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;

public interface Did {
    
    @UNIQUE_KEY
    @COLUMN_SIZE(1024)
    String getDid();
    void setDid(String did);
    
    @IS_NULLABLE(value = false)
    String getName();
    void setName(String name);
    
}
