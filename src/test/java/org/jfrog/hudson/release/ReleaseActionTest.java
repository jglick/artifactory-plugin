/*
 * Copyright (C) 2011 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.hudson.release;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the {@link ReleaseAction}.
 *
 * @author Yossi Shaul
 */
@Test
public class ReleaseActionTest {

    private ReleaseAction action;

    @BeforeMethod
    public void setup() {
        action = new MavenReleaseAction(null);
    }


    public void nextVersionSimpleMinor() {
        assertEquals(action.calculateNextVersion("1.2"), "1.3-SNAPSHOT");
        assertEquals(action.calculateNextVersion("3.2.12"), "3.2.13-SNAPSHOT");
    }

    public void nextVersionNoMinor() {
        assertEquals(action.calculateNextVersion("1"), "2-SNAPSHOT");
        assertEquals(action.calculateNextVersion("938"), "939-SNAPSHOT");
    }

    public void nextVersionCompoundMinor() {
        assertEquals(action.calculateNextVersion("1.2.3-4"), "1.2.3-5-SNAPSHOT");
    }

    public void unsupportedVersions() {
        // currently unsupported until custom/improved logic implementation
        assertEquals(action.calculateNextVersion("1-beta"), "Next.Version-SNAPSHOT");
        assertEquals(action.calculateNextVersion("1.2-alpha"), "Next.Version-SNAPSHOT");
    }
}
