# VeoAppChallenge - David Hagen

App Capabilities:

- MapsActivity

  - On launch the app loads a Google Maps fragment and will request the user for location permissions (both Coarse and Fine)

  - Users can tap a location on the map and the app will draw a polyline to the location based on the response from the Google Directions API

  - The start button will be enabled at this point, when the user clicks it the app will begin tracking their location and drawing their path on the map

  - Once the user arrives at the location or they click the "End Trip" button, they will be prompted by a dialog to store their trip data

  - After a trip is ended, the activity will still show their directions route polyline, taken path polyline, time, and distance traveled

  - The user can reset the map and select a new location
  
  - The user can start the ViewTripsActivity by clicking a floating action button

- ViewTripsActivity

  - This simply activity will display a recycler view to the user containing all of the stored Trips from the database
  
  - Users can tap individual rows and they will be prompted to delete them
  
  - Or users can tap the "Clear all" button to delete all stored Trips
---
### Development Decisions

In developing this app I utilized current conventions and industry best practices such as MVVM, Dagger for dependency injection, and more.
I knew I would use the Google Maps API as the base driver due to Google's comprehensive documentation and integration.
I have worked with Google's location APIs before, but not to this level so I dove into their documentation to familiarize myself with real time location tracking and drawing polylines.

Once I had an idea of what exact API calls I would need to do I began creating the ApiClient using Retrofit to handle the calls themselves, and Dagger2 to handle providing access to the client from the activities.
I used Postman to test the call I was trying to build to determine what information I needed to parse out of the response.
I then created the necessary Model classes to handle this data.
Finally, I implemented the polyline creation from the overview polyline element from the response.

Once that was done I began working on drawing the real time path the user was taking, this required a bit of experimenting with different location update parameters (interval, accuracy, and smallestDisplacement).
I settled on updating every 3 seconds with high accuracy and a displacement of 3 meters. I believe Veo currently uses a 5 second interval before scanning a scooter, but I'm not sure what the interval is while a ride is active.
There are two ways the trip will end, either the user gets within ~15 meters of their destination or they click the "End Trip" button.
When a trip is ended, the user will be prompted to store their trip data.

This storage is implemented using Room. Dagger is used to provide the DAO to the activities.
The data class for the trip is the TripEntity class. The fields being stored that end up displayed to the user are duration, distance, and start time.
The DAO contains methods to insert a trip, get a list of all trips, and delete either 1 or all trips.

Once the storage was finished I began working on the second activity that would allow the users to view their stored trips.
To start this activity, the user can click on a floating action button showing a floppy disk icon.
This activity shows the full list of trips stored in the database in a recycler view.
I also added an average speed text view that is calculated based on the totalDistance (in meters) and the duration (in second) to be displayed in miles per hour.
The user can tap on a recycler view list item to bring up a dialog asking if they would like to delete it. Or they can tap on the "Clear All Trips" button to delete every Trip at once.

---
### Dependencies
- Google maps play-services, maps-services - The play-services are required to track location in real time and control the map fragment

- Coroutines - Used to access the database asynchronously in the background to not hold up the UI thread

- Retrofit - Allows us to make calls to the Directions API via a custom client I wrote. Also includes the Gson converter for database type converting

- Dagger - Dependency injection

- RxJava adapter - Used to handle observables/live data

- Room - Database library. I had to use their latest release candidate build (rc01) due to a bug present in their latest stable release that causes the library to break when developing on an Apple ARM chip.
  - https://issuetracker.google.com/issues/174695268?pli=1
  
- RecyclerView - Ensure we have latest version, used to display live data from the database

---
### Demo Video
[Demo Video Link](https://youtu.be/ge94wBzJ8BU)

---
### Screenshots
Get permission dialog

![Get permission](https://user-images.githubusercontent.com/11951650/145724322-950460d1-9e17-4040-951e-3604533de2cf.png)

Initial screen when app is opened

![Initial screen](https://user-images.githubusercontent.com/11951650/145724334-1bf404a6-bebe-49f8-b585-9976a2cf7ae2.png)

After user taps the map

![Path Mapped](https://user-images.githubusercontent.com/11951650/145724433-58c61df8-5727-4c97-970c-f4fae545768d.png)

While the trip is active/being tracked

![Trip Active](https://user-images.githubusercontent.com/11951650/145724443-743d0a6f-0564-47d4-b687-8df0f38af08c.png)

Dialog prompt to store the trip

![Store Trip](https://user-images.githubusercontent.com/11951650/145724450-9f6851cc-6181-40d3-b486-3602339b65ce.png)

The list of all trips

![Trip List](https://user-images.githubusercontent.com/11951650/145724462-defc3efd-1234-4568-b6b1-ad73c6370633.png)

Dialog prompt to delete trip

![Delete Trip Prompt](https://user-images.githubusercontent.com/11951650/145724471-39ca514f-1347-4579-a603-eae34ebf1a78.png)



