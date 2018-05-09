package io.novaordis.playground.swim.simplest;

import swim.api.AbstractService;
import swim.api.SwimLane;
import swim.api.ValueLane;

/**
 * Models a simple source of data: a device of type A. Multiple physical instances of the device may be deployed.
 * Each instance of the device exposes a metric, which can be expressed as an integer.
 */
public class A extends AbstractService {

    @SwimLane("metric")
    private ValueLane<Integer> metric = valueLane().valueClass(Integer.class).
            didSet((newValue, oldValue) -> {

                System.out.println("metric value changed from " + oldValue + " to " + newValue);
            });

}
