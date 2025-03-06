package com.mongodb.javabasic.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MongoCacheConfig {    
    private String collectionName;
    private long ttl;
    private boolean flushOnBoot;
    private boolean storeBinaryOnly;
}
