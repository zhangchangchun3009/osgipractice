# osgipractice
osgi learning

## install equinox    
    download equinox sdk from this eclipse website: https://download.eclipse.org/equinox/
    
## develop run
    eclipse->Run->Run Configuration->new osgi framework->choose this workspace project,choose bundels from your installed equinox sdk platform.unloaded sdk of this project is of version '4.24',the newest version up to date 2022/6/26.    
    those bundles are always required:    
    1. org.eclipse.equinox.console_1.4.500.v20211021-1418.jar,
    2. org.apache.felix.gogo.command_1.1.2.v20210111-1007.jar,
    3. org.apache.felix.gogo.runtime_1.1.4.v20210111-1007.jar,
    4. org.apache.felix.gogo.shell_1.1.4.v20210111-1007.jar,
    5. org.eclipse.osgi_3.18.0.v20220516-2155.jar,
    6. org.eclipse.osgi.services_3.10.200.v20210723-0643.jar,
    7. org.osgi.util.function_1.2.0.202109301733.jar,
    8. org.osgi.util.promise_1.2.0.202109301733.jar
    click run,the osgi console appears indicates running is successful.
    try type command 'ss',and the self-define command 'dict check osgi' to call dictionary service. see help command
## deploy
    run maven build in the project root directory: mvn clean install   (we don't use eclipse build);    
    copy bundle out from target dir and to work dir '/equinox-sdk-/plugin';    
    edit 'equinox-sdk-/configuration/config.ini',add your bundles;    
    run equinox console in the directory '/equinox-sdk-':java -jar org.eclipse.osgi_3.18.0.v20220516-2155.jar -console;     
    


