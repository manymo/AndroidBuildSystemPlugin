/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.manymo;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper to run manymo commands.
 */
public class CommandHelper {

    private static final Lock sLock = new ReentrantLock();

    @NonNull
    private final String adb;
    private final ILogger logger;

    public CommandHelper(@NonNull String adb, @NonNull ILogger logger) {
        this.adb = adb;
        this.logger = logger;
    }

    public List<String> runCommand(@Nullable String log, @NonNull String... args)
            throws InterruptedException {
        String[] commandArray = new String[args.length + 3];
        commandArray[0] = "manymo";
        commandArray[1] = "--adb-path";
        commandArray[2] = adb;
        System.arraycopy(args, 0, commandArray, 3, args.length);

        List<String> stdout = Lists.newArrayList();
        List<String> stderr = Lists.newArrayList();

        // loop until we can lock. This allows other commands (including shutdown)
        // to run ahead of all the launch commands.
        // Otherwise we may end up with all the launch taking forever to run, and having
        // finished device wait to be able to be killed.
        //noinspection StatementWithEmptyBody
        while (!sLock.tryLock() && !sLock.tryLock(1000, TimeUnit.MILLISECONDS));

        try {
            if (log != null) {
                logger.info(log);
            }
            logger.verbose("COMMAND: " + Arrays.toString(commandArray));
            Process process = Runtime.getRuntime().exec(commandArray);
            if (grabProcessOutput(process, stdout, stderr) != 0) {
                String msg = String.format("Failed to run command '%s':\n%s",
                        Joiner.on(' ').join(commandArray),
                        Joiner.on('\n').join(stderr));
                logger.error(null, msg);
                throw new RuntimeException(msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sLock.unlock();
        }

        return stdout;
    }

    private static int grabProcessOutput(
            @NonNull final Process process,
            @NonNull final List<String> stdout,
            @NonNull final List<String> stderr) throws InterruptedException {
        // read the lines as they come. if null is returned, it's
        // because the process finished
        Thread threadErr = new Thread("stderr") {
            @Override
            public void run() {
                // create a buffer to read the stderr output
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line == null) {
                            break;
                        }
                        stderr.add(line);
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        Thread threadOut = new Thread("stdout") {
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);

                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line == null) {
                            break;
                        }
                        stdout.add(line);
                    }
                } catch (IOException e) {
                    // do nothing.
                }
            }
        };

        threadErr.start();
        threadOut.start();

        // it looks like on windows process#waitFor() can return
        // before the thread have filled the arrays, so we wait for both threads and the
        // process itself.
        threadErr.join();
        threadOut.join();

        // get the return code from the process
        return process.waitFor();
    }
}
