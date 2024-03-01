#### Nume: Titoc Oana-Alexandra
#### Grupa: 323CA
###### Am folosit scheletul de cod pus la dispozitie

## Design Patterns Used

1. `Singleton Pattern`

    The purpose of using the Singleton pattern is to ensure that there's only
    one instance of the Admin class in the entire application, and it provides a
    global point of access to that instance.


2. `Command Pattern`

    I used Command Pattern to encapsulate the actions of moving to the next page
    and the previous page. By applying the Command Pattern in this scenario, I
    achieved a more modular and flexible design, making it easier to manage and
    extend the functionality related to page navigation.


3. `Factory Pattern`

    The Factory Pattern provides a structured and flexible approach to create
    different types of pages, offering benefits in terms of abstraction,
    encapsulation, centralized control, extensibility, code readability, and
    reusability. There are different types of pages: HomePage, ArtistPage,
    HostPage, etc. Each of these pages has its own creation logic based on the
    type of user or context. By employing the Factory Pattern I abstracted the
    creation logic of these pages into dedicated factory classes (HomePageFactory,
    LikedContentPageFactory, ArtistPageFactory, HostPageFactory).


4. `Observer Pattern`

    I used the Observer Design Pattern for the Notification system. I have a Notification
    class acting as the subject (also known as the observable), and a NotificationObserver
    interface that observers implement to receive notifications. The NotificationListManager
    class is a concrete observer that manages a list of notifications and reacts to updates
    from the subject. By using the Observer Pattern, I've created a modular and flexible
    system for handling notifications, making it easier to manage and extend in case the
    application evolves.

## Structure of the Project
#### -classes added

* `src/`
    * `app/`
      * `pages/`
        * `CommandNextPrev/` &rarr; the package with the classes needed for the Command Pattern
          * `Command.java` &rarr; the interface for a command
          * `NextPage.java` &rarr; implements Command, used for the forward navigation
          * `Page.java`
          * `Page.java` &rarr; implements Command, used for the backward navigation
        * `FactoryPages/` &rarr; the package with the classes needed for the Factory Pattern
          * `ArtistPageFactory.java` &rarr; implements PageFactory, used for an artist page creation
          * `HomePageFactory.java` &rarr; implements PageFactory, used for a home page creation
          * `HostPageFactory` &rarr; implements PageFactory, used for a host page creation
          * `LikedContentPageFactory` &rarr; implements PageFactory, used for a liked content page creation
          * `PageFactory` &rarr; the interface for a factory that creates instances of Page.
      * `user/`
        * `Entities/` &rarr; the package that contains all entities needed by all types of users
          * `Notifications/` &rarr; the package containing all classes needed to implement Observer Pattern
            * `Notification.java` &rarr; the class containing the name and description of a notification
            * `NotificationListManager.java` &rarr; implements NotificationObserver, implements the update and display modifications methods
            * `NotificationObserver.java` &rarr; the interface for managing notifications with Observer Design Pattern
          * `AdBreak.java` &rarr; the class that manipulate ads datas
          * `Announcement`
          * `Event`
          * `Merchandise`
        * `Statistics/` &rarr; the package with user/artist/host statistics
          * `Infos.java` &rarr; the class used to populate all datas needed for "wrapped" command
          * `WrappedArtist.java` &rarr; the class containing all the statistics to be listed for the artist
          * `WrappedHost.java` &rarr; the class containing all the statistics to be listed for the host
          * `WrappedUser.java` &rarr; the class containing all the statistics to be listed for the user
        * `Subscribe.java` &rarr; the class that manages the names of the artists/hosts of whom the user has subscribed and the list of notifications
  

    


## Program Flow
The User, Artist, and Host classes have been introduced with new fields to manage their data, particularly the statistics. The retention of user statistics in the system follows the this logic: the statistics of all users are updated with each user load and then final one at the end. With each given load, the statistics are analyzed from the previous load up to the present, using a copy of the player (ensuring access to previously loaded content even if the original player is no longer active). Every time the updateStatistics function is called, it checks the type of source the user had in the previous load (album, song, podcast) and updates the statistics accordingly for both the user and the artist or host, as applicable. The updateStatistics function can be called between two successive loads, updating incrementally. The function depends on the last timestamp it was called, without affecting what has already been loaded. In this function we take care of the
monetization as well. The songs for user premium and user free are updated too, leading to a double use.

The notification system is implemented with Observer Design Pattern which makes it more easy to understand. Every time an album, a merch or an event is added, the notification for that artist is updated. 

The page navigation system is implemented by Command Design Pattern and the page creation with Factory Design Pattern.



