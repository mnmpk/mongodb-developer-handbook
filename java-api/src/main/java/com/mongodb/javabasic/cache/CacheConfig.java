package com.mongodb.javabasic.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheConfig {    
    private String collectionName;
    private long ttl;
    private boolean flushOnBoot;
    private boolean storeBinaryOnly;
}
