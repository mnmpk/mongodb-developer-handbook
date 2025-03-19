package com.mongodb.javabasic.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheConfig {    
    private String cacheName;
    private long ttl;
    private boolean flushOnBoot;
    private boolean storeBinaryOnly;
}
