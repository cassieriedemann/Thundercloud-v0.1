package boombotix.com.thundercloud.dependencyinjection.component;

import javax.inject.Singleton;

import boombotix.com.thundercloud.ThundercloudApplication;
import boombotix.com.thundercloud.dependencyinjection.ThundercloudGraph;
import boombotix.com.thundercloud.dependencyinjection.module.ApiModule;
import boombotix.com.thundercloud.dependencyinjection.module.ApplicationModule;
import boombotix.com.thundercloud.dependencyinjection.module.BluetoothModule;
import boombotix.com.thundercloud.dependencyinjection.module.OverrideBluetoothModule;
import boombotix.com.thundercloud.dependencyinjection.module.RepositoryModule;
import dagger.Component;

/**
 * Application level injection class (Component). The lifetime of this component is the lifetime of
 * the application, and any global modules should be defined as part of this module.
 *
 * For Modules/injections that only need to live through a given activity lifecycle (which should in
 * theory be anything which is not strictly global), see {@link ActivityComponent}
 *
 * Created by kenton on 1/24/16.
 */
@Singleton
@Component(modules =
        {
                ApplicationModule.class,
                RepositoryModule.class,
                ApiModule.class,
                BluetoothModule.class
        }
)
public interface ApplicationComponent extends ThundercloudGraph {


    final class Initializer {

        public static ApplicationComponent init(ThundercloudApplication application) {
            return DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(application))
                    .apiModule(new ApiModule())
                    .bluetoothModule(new OverrideBluetoothModule())
                    .repositoryModule(new RepositoryModule(application))
                    .build();
        }

        private Initializer() {
            // Block instantiation
        }
    }
}
