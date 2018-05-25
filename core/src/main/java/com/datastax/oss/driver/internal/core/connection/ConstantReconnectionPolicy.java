/*
 * Copyright DataStax, Inc.
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
package com.datastax.oss.driver.internal.core.connection;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigProfile;
import com.datastax.oss.driver.api.core.connection.ReconnectionPolicy;
import com.datastax.oss.driver.api.core.context.DriverContext;
import java.time.Duration;

/** A reconnection policy that waits a constant time between each reconnection attempt. */
public class ConstantReconnectionPolicy implements ReconnectionPolicy {
  private final long delayMs;

  /** Builds a new instance. */
  public ConstantReconnectionPolicy(DriverContext context) {
    DriverConfigProfile config = context.config().getDefaultProfile();
    long constantDelayMs =
        config.getDuration(DefaultDriverOption.RECONNECTION_BASE_DELAY).toMillis();
    if (constantDelayMs < 0)
      throw new IllegalArgumentException(
          String.format(
              "Invalid negative delay for "
                  + DefaultDriverOption.RECONNECTION_BASE_DELAY.getPath()
                  + " (got %d)",
              constantDelayMs));

    this.delayMs = constantDelayMs;
  }

  @Override
  public ReconnectionSchedule newSchedule() {
    return new ConstantSchedule();
  }

  @Override
  public void close() {
    // nothing to do
  }

  private class ConstantSchedule implements ReconnectionSchedule {

    @Override
    public Duration nextDelay() {
      return Duration.ofMillis(delayMs);
    }
  }
}
