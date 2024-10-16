#!/bin/bash
mongosh mongodb://localhost:27017 -u root -p root <<EOF
use finesdb;

db.createCollection("fine");

#db.fine.insertMany([
#                   {
#                       "car": {
#                           "plate": "BB1234CC",
#                           "make": "Honda",
#                           "model": "Civic",
#                           "color": "BLACK"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4520, 30.5250]
#                               },
#                               "dateTime": "2023-09-05T09:20:30.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/AB2565EP_570080547.jpg",
#                               "violations": [
#                                   {
#                                       "description": "VIOLATION OF SIGNS",
#                                       "price": 340.0
#                                   },
#                                   {
#                                       "description": "PARKED IN FORBIDDEN AREAS",
#                                       "price": 680.0
#                                   },
#                                   {
#                                       "description": "OBSTRUCTS TRAFFIC PEDESTRIANS",
#                                       "price": 680.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "CC1234DD",
#                           "make": "Ford",
#                           "model": "Fusion",
#                           "color": "SILVER"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4530, 30.5260]
#                               },
#                               "dateTime": "2023-09-12T12:10:30.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/AC7136CM_572352720.jpg",
#                               "violations": [
#                                   {
#                                       "description": "VIOLATES PARKING SCHEME",
#                                       "price": 680.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "DD1234EE",
#                           "make": "Chevrolet",
#                           "model": "Cruze",
#                           "color": "RED"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4540, 30.5270]
#                               },
#                               "dateTime": "2023-09-18T14:15:30.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/AI0385EA_572342014.jpg",
#                               "violations": [
#                                   {
#                                       "description": "PARKED IN DISABLED ZONE",
#                                       "price": 1700.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "FF1234GG",
#                           "make": "Nissan",
#                           "model": "Altima",
#                           "color": "GREEN"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4550, 30.5280]
#                               },
#                               "dateTime": "2023-09-22T16:25:30.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/AX2594BM_572339085.jpg",
#                               "violations": [
#                                   {
#                                       "description": "OBSTRUCTS MUNICIPAL TRANSPORT MOVEMENT",
#                                       "price": 680.0
#                                   },
#                                   {
#                                       "description": "PARKED ON BIKE LANE",
#                                       "price": 680.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "GG1234HH",
#                           "make": "Hyundai",
#                           "model": "Elantra",
#                           "color": "BROWN"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4560, 30.5290]
#                               },
#                               "dateTime": "2023-09-27T11:05:30.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/BC4631PE_572320190.jpg",
#                               "violations": [
#                                   {
#                                       "description": "PARKED IN TWO LANES",
#                                       "price": 680.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "HH1234II",
#                           "make": "Volkswagen",
#                           "model": "Passat",
#                           "color": "YELLOW"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4570, 30.5300]
#                               },
#                               "dateTime": "2023-09-10T10:30:45.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/BC6727OE_565621483.jpg",
#                               "violations": [
#                                   {
#                                       "description": "PARKED ON PUBLIC TRANSPORT LANE",
#                                       "price": 680.0
#                                   }
#                               ]
#                           }
#                       ]
#                   },
#                   {
#                       "car": {
#                           "plate": "KK1234LL",
#                           "make": "Mercedes",
#                           "model": "C-Class",
#                           "color": "BLACK"
#                       },
#                       "trafficTickets": [
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4590, 30.5320]
#                               },
#                               "dateTime": "2023-09-29T13:50:45.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/BH2393TH_572350178.jpg",
#                               "violations": [
#                                   {
#                                       "description": "VIOLATION OF LICENSE PLATE USE",
#                                       "price": 1190.0
#                                   },
#                                   {
#                                       "description": "VIOLATES PARKING SCHEME",
#                                       "price": 680.0
#                                   }
#                               ]
#                           },
#                           {
#                               "location": {
#                                   "type": "Point",
#                                   "coordinates": [50.4580, 30.5310]
#                               },
#                               "dateTime": "2023-09-01T12:40:45.938Z",
#                               "photoUrl": "http://localhost:4566/fine-car-images/BX0251EK_567828344.jpg",
#                               "violations": [
#                                    {
#                                        "description": "OBSTRUCTS TRAFFIC PEDESTRIANS",
#                                        "price": 680.0
#                                    },
#                                    {
#                                        "description": "PARKED IN DISABLED ZONE",
#                                        "price": 1700.0
#                                    }
#                               ]
#                           }
#                       ]
#                   }
#               ]
#
#);
EOF
