package com.tenpearls.contactmanagementsystem.constant;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstantTest {

    @Test
    void constructor_throwsException_whenInstantiated() throws NoSuchMethodException {
        Constructor<Constant> constructor = Constant.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertThrows(IllegalStateException.class, () -> { throw thrown.getCause(); });
    }
}