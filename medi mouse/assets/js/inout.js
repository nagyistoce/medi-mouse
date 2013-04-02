
var inout = {
		state: {
			path:[],
			extra:{},
		},
		setup: function(id){
			var root = $('#'+id);
			for(item in menu){
				var newItem = $('<div></div>');
				newItem.attrs('id',item);
				newItem.html(item);
				newItem.click(inout.itemClick(menu[item],[item]));
			}
		},
		itemClick: function(children,)
		resize: function (height,width){
			
		},
		stackAndDisplay: function(selected,children,siblings,path){
			
			
		},
		buildDateSelector: function(){
			

		},
		drawItem:function(name,pos){
			
		},
		save: function(path){
			
		}
		
};
var menu = {
		"In": {
	        "Unavailable": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        },
	        "En route": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        },
	        "Outside": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        },
	        "Can be phoned": {
	            "Minnetonka": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0014kwmptk.mthd",
	                        "name": "in2"
	                    }
	                }
	            },
	            "Eden Prairie": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0015lnnhlh.mthd",
	                        "name": "in3"
	                    }
	                }
	            },
	            "Norwood": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0016m3oxf4.mthd",
	                        "name": "in4"
	                    }
	                }
	            },
	            "Working at home": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0017nvs5lv.mthd",
	                        "name": "in5"
	                    }
	                }
	            },
	            "Foxborough": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0018oyiqcm.mthd",
	                        "name": "in6"
	                    }
	                }
	            },
	            "Canton": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno0019p82y2l.mthd",
	                        "name": "in7"
	                    }
	                }
	            },
	            "Working at site": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001aqpnnzn.mthd",
	                        "name": "in8"
	                    }
	                }
	            },
	            "Atlanta": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001br2a5en.mthd",
	                        "name": "in9"
	                    }
	                }
	            },
	            "Framingham": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001cswz6ce.mthd",
	                        "name": "in10"
	                    }
	                }
	            },
	            "Southcoast": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001dteal9x.mthd",
	                        "name": "in11"
	                    }
	                }
	            },
	            "Westwood": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001fvuzv39.mthd",
	                        "name": "in13"
	                    }
	                }
	            },
	            "Lowder Brook": {
	                "Save": "Save",
	                "extra": {
	                    "phone_ext_name": {
	                        "link": "/lc8o1fno001eu5447b.mthd",
	                        "name": "in12"
	                    }
	                }
	            }
	        },
	        "At my desk": {
	            "Save": "Save"
	        },
	        "In the cafe": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        },
	        "Can be paged": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        },
	        "In a meeting": {
	            "Minnetonka": {
	                "Save": "Save"
	            },
	            "Eden Prairie": {
	                "Save": "Save"
	            },
	            "Norwood": {
	                "Save": "Save"
	            },
	            "Working at home": {
	                "Save": "Save"
	            },
	            "Foxborough": {
	                "Save": "Save"
	            },
	            "Canton": {
	                "Save": "Save"
	            },
	            "Working at site": {
	                "Save": "Save"
	            },
	            "Atlanta": {
	                "Save": "Save"
	            },
	            "Framingham": {
	                "Save": "Save"
	            },
	            "Southcoast": {
	                "Save": "Save"
	            },
	            "Westwood": {
	                "Save": "Save"
	            },
	            "Lowder Brook": {
	                "Save": "Save"
	            }
	        }
	    },
	    "Out": {
	        "Out on leave": {
	            "Save": "Save",
	            "extra": {
	                "date_select_name": {
	                    "link": "/lc8o1fno002jisoicu.mthd",
	                    "name": "in14"
	                }
	            }
	        },
	        "Back in 1/2 hour": {
	            "Save": "Save"
	        },
	        "Out on vacation": {
	            "Save": "Save",
	            "extra": {
	                "date_select_name": {
	                    "link": "/lc8o1fno002kkywzh7.mthd",
	                    "name": "in15"
	                }
	            }
	        },
	        "Out sick": {
	            "Save": "Save",
	            "extra": {
	                "date_select_name": {
	                    "link": "/lc8o1fno002l4mxyc4.mthd",
	                    "name": "in16"
	                }
	            }
	        },
	        "Back in a while": {
	            "Save": "Save"
	        },
	        "Out for the day": {
	            "Save": "Save",
	            "extra": {
	                "date_select_name": {
	                    "link": "/lc8o1fno002m6k4ef4.mthd",
	                    "name": "in17"
	                }
	            }
	        },
	        "Back in 1 hour": {
	            "Save": "Save"
	        }
	    }
};