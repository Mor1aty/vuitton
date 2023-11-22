package com.moriaty.vuitton.service.view;

import com.moriaty.vuitton.bean.view.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 模块
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:55
 */
@Component
@Slf4j
public class ModuleFactory {

    private ModuleFactory() {

    }

    private static List<Module> factoryModuleList = new ArrayList<>();

    public static void addModule(Module module) {
        factoryModuleList.add(module);
        sort();
    }

    public static void addModule(List<Module> moduleList) {
        factoryModuleList.addAll(moduleList);
        sort();
    }

    public static Module getModule(int index) {
        return factoryModuleList.get(index);
    }

    public static List<Module> getAllModule() {
        return factoryModuleList;
    }

    private static void sort() {
        factoryModuleList = factoryModuleList.stream().sorted(Comparator.comparing(Module::getId))
                .collect(Collectors.toList());
    }
}
