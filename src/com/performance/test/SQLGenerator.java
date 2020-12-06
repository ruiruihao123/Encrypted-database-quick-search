package com.performance.test;

public class SQLGenerator {

	public String sqlCreateStudent(){	
		return "create table student(id int,name varchar(20),age int,sex char(5));";
	}
	
	public String sqlCreateGradeCryptDB(){
		return "create table grade(id int,grade int);";
	}
	
	public String sqlCreateGradeSSDB(){
		return "create table grade(id int,grade double);";
	}
	
	public String sqlInsertToStudent(int n){
		StringBuilder insert =  new StringBuilder();
		insert.append("insert into student(id,name,age,sex) values");		
		for(int index = 0;index < n-1;index++){
			int id = index+1;
			String name = "wang";
			int age = index+1;
			String sex = "boy";
			String eachValue = "("+id+","+"'"+name+"'"+","+age+","+"'"+sex+"'"+"),";
			insert.append(eachValue);
		}
		{
			int id = n;
			String name = "wang";
			int age = n;
			String sex = "boy";
			String eachValue = "("+id+","+"'"+name+"'"+","+age+","+"'"+sex+"'"+");";
			insert.append(eachValue);
		}
		return insert.toString();
	}
	public String sqlInsertToGrade(int n){
		StringBuilder insert =  new StringBuilder();
		insert.append("insert into grade(id,grade) values");		
		for(int index = 0;index < n-1;index++){
			int id = index+1;
			int grade = index+1;
			String eachValue = "("+id+","+grade+"),";
			insert.append(eachValue);
		}
		{
			int id = n;
			int grade = n;
			String eachValue = "("+id+","+grade+");";
			insert.append(eachValue);
		}
		return insert.toString();
	}
	
	
	public String sqlUpdateStudent(int n){
		return null;
	}
	public String sqlUpdateGrade(int n){
		return null;
	}
	
	
	public String sqlDeleteStudent(int n){
		return null;
	}
	
	public String sqlDeleteGrade(int n){
		return null;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SQLGenerator sg = new SQLGenerator();
		System.out.println(sg.sqlInsertToStudent(10));
		System.out.println(sg.sqlInsertToGrade(10));
		System.out.println(sg.sqlCreateStudent());
		System.out.println(sg.sqlCreateGradeCryptDB());
		System.out.println(sg.sqlCreateGradeSSDB());
	}

}
