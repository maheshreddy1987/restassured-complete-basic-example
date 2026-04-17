/*
 * MIT License
 *
 * Copyright (c) 2020 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.eliasnogueira.credit.commons;

import java.math.BigDecimal;
import java.util.HashMap;

public final class PayloadGenerator {

    private PayloadGenerator() {
    }

    public static HashMap<String, Object> fromEntries(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Entries must contain key/value pairs");
        }

        HashMap<String, Object> payload = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            if (!(key instanceof String keyName)) {
                throw new IllegalArgumentException("Key must be a String");
            }

            payload.put(keyName, keyValuePairs[i + 1]);
        }
        return payload;
    }

    public static HashMap<String, Object> simulationPayload(String name, String cpf, String email,
                                                            BigDecimal amount, Integer installments,
                                                            Boolean insurance) {
        return fromEntries(
            "name", name,
            "cpf", cpf,
            "email", email,
            "amount", amount,
            "installments", installments,
            "insurance", insurance
        );
    }
}
