package org.springbud.orm.support;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TableDefinition {

    private String name;

    // Map for record (type - name)
    private Map</*type*/String, /* name*/String> record;
}
