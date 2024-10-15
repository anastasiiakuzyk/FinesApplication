#!/bin/bash
mongosh mongodb://localhost:27017 -u root -p root <<EOF
use finesdb;

db.createCollection("fine");

db.fine.insertMany([
                   {
                       "car": {
                           "plate": "BB1234CC",
                           "make": "Honda",
                           "model": "Civic",
                           "color": "BLACK"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4520, 30.5250]
                               },
                               "dateTime": "2023-09-05T09:20:30.938Z",
                               "photoUrl": "https://example.com/photo3",
                               "violations": [
                                   {
                                       "description": "VIOLATION OF SIGNS",
                                       "price": 340.0
                                   },
                                   {
                                       "description": "PARKED IN FORBIDDEN AREAS",
                                       "price": 680.0
                                   },
                                   {
                                       "description": "OBSTRUCTS TRAFFIC PEDESTRIANS",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "CC1234DD",
                           "make": "Ford",
                           "model": "Fusion",
                           "color": "SILVER"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4530, 30.5260]
                               },
                               "dateTime": "2023-09-12T12:10:30.938Z",
                               "photoUrl": "https://example.com/photo4",
                               "violations": [
                                   {
                                       "description": "VIOLATES PARKING SCHEME",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "DD1234EE",
                           "make": "Chevrolet",
                           "model": "Cruze",
                           "color": "RED"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4540, 30.5270]
                               },
                               "dateTime": "2023-09-18T14:15:30.938Z",
                               "photoUrl": "https://example.com/photo5",
                               "violations": [
                                   {
                                       "description": "PARKED IN DISABLED ZONE",
                                       "price": 1700.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "EE1234FF",
                           "make": "Mazda",
                           "model": "6",
                           "color": "BLUE"
                       },
                       "trafficTickets": []
                   },
                   {
                       "car": {
                           "plate": "FF1234GG",
                           "make": "Nissan",
                           "model": "Altima",
                           "color": "GREEN"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4550, 30.5280]
                               },
                               "dateTime": "2023-09-22T16:25:30.938Z",
                               "photoUrl": "https://example.com/photo6",
                               "violations": [
                                   {
                                       "description": "OBSTRUCTS MUNICIPAL TRANSPORT MOVEMENT",
                                       "price": 680.0
                                   },
                                   {
                                       "description": "PARKED ON BIKE LANE",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "GG1234HH",
                           "make": "Hyundai",
                           "model": "Elantra",
                           "color": "BROWN"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4560, 30.5290]
                               },
                               "dateTime": "2023-09-27T11:05:30.938Z",
                               "photoUrl": "https://example.com/photo7",
                               "violations": [
                                   {
                                       "description": "PARKED IN TWO LANES",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "HH1234II",
                           "make": "Volkswagen",
                           "model": "Passat",
                           "color": "YELLOW"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4570, 30.5300]
                               },
                               "dateTime": "2023-09-10T10:30:45.938Z",
                               "photoUrl": "https://example.com/photo8",
                               "violations": [
                                   {
                                       "description": "PARKED ON PUBLIC TRANSPORT LANE",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "II1234JJ",
                           "make": "Subaru",
                           "model": "Legacy",
                           "color": "WHITE"
                       },
                       "trafficTickets": []
                   },
                   {
                       "car": {
                           "plate": "JJ1234KK",
                           "make": "Kia",
                           "model": "Optima",
                           "color": "SILVER"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4580, 30.5310]
                               },
                               "dateTime": "2023-09-01T12:40:45.938Z",
                               "photoUrl": "https://example.com/photo9",
                               "violations": [
                                   {
                                       "description": "OBSTRUCTS TRAFFIC PEDESTRIANS",
                                       "price": 680.0
                                   },
                                   {
                                       "description": "PARKED IN DISABLED ZONE",
                                       "price": 1700.0
                                   }
                               ]
                           }
                       ]
                   },
                   {
                       "car": {
                           "plate": "KK1234LL",
                           "make": "Mercedes",
                           "model": "C-Class",
                           "color": "BLACK"
                       },
                       "trafficTickets": [
                           {
                               "location": {
                                   "type": "Point",
                                   "coordinates": [50.4590, 30.5320]
                               },
                               "dateTime": "2023-09-29T13:50:45.938Z",
                               "photoUrl": "https://example.com/photo10",
                               "violations": [
                                   {
                                       "description": "VIOLATION OF LICENSE PLATE USE",
                                       "price": 1190.0
                                   },
                                   {
                                       "description": "VIOLATES PARKING SCHEME",
                                       "price": 680.0
                                   }
                               ]
                           }
                       ]
                   }
               ]

);
EOF
