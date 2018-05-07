/**
 * This file contains helpers for c++ code
 */

#ifndef EXPERIMAESTRO_CPP_HPP
#define EXPERIMAESTRO_CPP_HPP

#include "common.hpp"
#include "xpm.hpp"
#include "value.hpp"
#include "register.hpp"

namespace xpm {

ptr<Register> currentRegister();
void currentRegister(ptr<Register> const &_register);

template<typename T>  struct type_of {};


template<typename T>
struct ArgumentHolder {
  virtual void setValue(T &self, xpm::StructuredValue::Ptr const &value) = 0;
};

// inline void assignValue(xpm::StructuredValue::Ptr const &value, std::string &s) {
//   s = value->asString();
// }
// inline void assignValue(xpm::StructuredValue::Ptr const &value, int &x) {
//   x = value->asInteger();
// }
// inline void assignValue(xpm::StructuredValue::Ptr const &value, long &x) {
//   x = value->asInteger();
// }
// inline void assignValue(xpm::StructuredValue::Ptr const &value, Path &s) {
//   s = Path(value->asString());
// }

template<typename T>
inline void assignValue(xpm::StructuredValue::Ptr const &value, ptr<T> &p) {
  p = std::dynamic_pointer_cast<T>(value);
  if (!p && value) {
    throw xpm::argument_error(std::string("Expected ") + type_of<ptr<T>>::value()->toString()
                                  + " but got " + value->type()->toString());
  }
}


template<typename T, typename Value>
struct TypedArgumentHolder : public ArgumentHolder<T> {
  Value T::* valuePtr;

  TypedArgumentHolder(Value T::* valuePtr) : valuePtr(valuePtr) {}

  virtual void setValue(T &self, xpm::StructuredValue::Ptr const &value) override {
    xpm::assignValue(value, (&self)->*valuePtr);
  }
};


template<typename T>
struct CppType  {
  static_assert(std::is_base_of<StructuredValue, T>::value,  "Type should be a subtype of CppObject");
  ptr<Type> type;
  std::unordered_map<std::string, std::unique_ptr<ArgumentHolder<T>>> _arguments;

  static ptr<CppType<T>> SELF;

  CppType(std::string const &name) : type(std::make_shared<Type>(TypeName(name))) {
  }
};
template<typename T> ptr<CppType<T>>
    CppType<T>::SELF;

/// Type of a variable
template<typename T>  struct type_of<ptr<T>> {
  static ptr<Type> value() {
    return CppType<T>::SELF->type;
  }
};


#define XPM_SIMPLETYPE_OF(CPPTYPE, XPMTYPE) \
  template<> struct type_of<CPPTYPE> { static ptr<Type> value() { return XPMTYPE; } };

XPM_SIMPLETYPE_OF(std::string, StringType);
XPM_SIMPLETYPE_OF(bool, BooleanType);
XPM_SIMPLETYPE_OF(long, IntegerType);
XPM_SIMPLETYPE_OF(int, IntegerType);
XPM_SIMPLETYPE_OF(float, RealType);
XPM_SIMPLETYPE_OF(double, RealType);
XPM_SIMPLETYPE_OF(ptr<StructuredValue>, AnyType);
XPM_SIMPLETYPE_OF(Path, PathType);


template<typename T, typename Parent>
struct CppTypeBuilder {
  ptr<CppType<T>> type;
  ptr<Argument> _argument;
  ptr<Register> _register = currentRegister();

  CppTypeBuilder(std::string const &name) : type(std::make_shared<CppType<T>>(name)) {
    currentRegister()->addType(type->type);
    CppType<T>::SELF = type;

    if (!std::is_same<xpm::StructuredValue, Parent>::value) {
      type->type->parentType(type_of<ptr<Parent>>::value());
    }
  }

  template<typename U>
  CppTypeBuilder &argument(std::string const &name, U T::* u) {
    _argument = std::make_shared<Argument>(name);
    type->type->addArgument(_argument);
    type->_arguments[name] = std::unique_ptr<ArgumentHolder<T>>(new TypedArgumentHolder<T,U>(u));
    _argument->type(type_of<U>::value());
    return *this;
  }
  CppTypeBuilder &required(bool r) {
    _argument->required(r);
    return *this;
  }

  CppTypeBuilder &generator(ptr<Generator> const &g) {
    _argument->generator(g);
    return *this;
  }

  template<typename U>
  CppTypeBuilder &defaultValue(U const &v) {
    _argument->defaultValue(std::make_shared<Value>(v));
    return *this;
  }

  CppTypeBuilder &defaultJson(nlohmann::json const &j) {
    _argument->defaultValue(std::make_shared<StructuredValue>(*_register, j));
    return *this;
  }
  operator ptr<CppType<T>>() {
    return type;
  }
};

template<typename T, typename Parent = xpm::StructuredValue>
class CppObject : public Parent {
 public:
  static ptr<CppType<T>> XPM_TYPE;

  void setValue(std::string const &name, ptr<StructuredValue> const &value) override {
    auto it = XPM_TYPE->_arguments.find(name);
    if (it != XPM_TYPE->_arguments.end()) {
      T &t = dynamic_cast<T&>(*this);
      it->second->setValue(t, value);
    } else {
      Parent::setValue(name, value);
    }
  }
};

extern ptr<CommandPath> EXECUTABLE_PATH;

template<typename _Type, typename _Task>
struct TaskBuilder {

  static_assert(std::is_base_of<_Type, _Task>::value,  "Task should be a subclass of type");
  TaskBuilder(std::string const &tname) {
    auto task = std::make_shared<Task>(TypeName(tname), CppType<_Type>::SELF->type);

    auto commandLine = std::make_shared<CommandLine>();
    auto command = std::make_shared<Command>();
    command->add(EXECUTABLE_PATH);
    command->add(std::make_shared<CommandString>("run"));
    command->add(std::make_shared<CommandString>(tname));
    command->add(std::make_shared<CommandParameters>());
    commandLine->add(command);
    task->commandline(commandLine);
    currentRegister()->addTask(task);
  }
};

}

#define XPM_SIMPLETASK(tname, TYPE) xpm::TaskBuilder<TYPE, TYPE> TASK ## __LINE__ (tname);
#define XPM_TASK(tname, TYPE, TASK) xpm::TaskBuilder<TYPE, TASK> TASK ## __LINE__ (tname);

#define XPM_SUBTYPE(NAME, TYPE, PARENT) \
  template<> ptr<xpm::CppType<TYPE>> xpm::CppObject<TYPE, PARENT>::XPM_TYPE \
     = xpm::CppTypeBuilder<TYPE, PARENT>(NAME)
#define XPM_TYPE(NAME, TYPE) XPM_SUBTYPE(NAME, TYPE, xpm::Object)

#endif //PROJECT_CPP_HPP