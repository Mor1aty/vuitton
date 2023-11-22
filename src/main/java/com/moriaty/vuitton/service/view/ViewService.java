package com.moriaty.vuitton.service.view;

import com.moriaty.vuitton.bean.view.Module;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 视图 Service
 * </p>
 *
 * @author Moriaty
 * @since 2023/11/20 14:48
 */
@Service
@AllArgsConstructor
@Slf4j
public class ViewService {

    public List<Module> findAllModule() {
        return ModuleFactory.getAllModule();
    }
}
