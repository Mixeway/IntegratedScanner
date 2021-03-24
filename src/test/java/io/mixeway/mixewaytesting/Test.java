/*
 * @created  2021-01-26 : 12:17
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.mixewaytesting;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
public class Test {

    @org.junit.Test
    public void testvoid(){
        List<CisRequirement> cisRequirementList = new ArrayList<>();
        cisRequirementList.add(new CisRequirement("test1", "type1"));
        cisRequirementList.add(new CisRequirement("test2", "type2"));
        cisRequirementList.add(new CisRequirement("test3", "type3"));
        cisRequirementList.add(new CisRequirement("test4", "type4"));
        cisRequirementList.add(new CisRequirement("test5", "type5"));
        cisRequirementList.add(new CisRequirement("test6", "type6"));

        cisRequirementList.stream().forEach( cis -> cis.setSeverity(cis.getName() + " x "+cis.getType()));

        for (CisRequirement c : cisRequirementList){
            System.out.println("Name: "+c.getName()+", severity: "+c.getSeverity());
        }
    }

    @org.junit.Test
    public void test2(){
        List<CisRequirement> cisRequirementList = new ArrayList<>();
        cisRequirementList.add(new CisRequirement("test1", "type1"));
        cisRequirementList.add(new CisRequirement("test2", "type2"));
        cisRequirementList.add(new CisRequirement("test3", "x"));
        cisRequirementList.add(new CisRequirement("test4", null));
        cisRequirementList.add(new CisRequirement("test5", null));
        cisRequirementList.add(new CisRequirement("test6", null));

        for (CisRequirement iv : cisRequirementList) {
            if (iv.getType() == null || (iv.getType() != null && !iv.getType().equals("x"))) {
                System.out.println(iv.getName()+" jest ok");
            }
        }

    }

    @org.springframework.context.annotation.Configuration
    public static class ContextConfiguration {
    }
}
