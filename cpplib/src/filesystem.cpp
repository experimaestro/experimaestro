//
// Created by Benjamin Piwowarski on 19/11/2016.
//

#include <numeric>
#include <vector>
#include <xpm/filesystem.hpp>

namespace xpm {

template<>
struct Reference<Share> {
  std::string host;
  std::string name;
  Reference(const std::string &host, const std::string &name) : host(host), name(name) {}
};

template<>
struct Reference<Path> {
  std::string share;
  std::string node;
  std::string path;
  Reference() {}
  Reference(const std::string &share, const std::string &node, const std::string &path)
      : share(share), node(node), path(path) {}
};

Share::Share(std::string const &host, std::string const &name) :
    Pimpl(host, name) {

}
Path::Path(std::string const &pathstring) : Pimpl("", "", pathstring) {
}

Path::Path() : Pimpl("", "", "/")  {
}

Path::Path(Path const &parent, std::initializer_list<std::string> const &relative) :
    Pimpl(parent._this->share, parent._this->node,
          std::accumulate(relative.begin(), relative.end(), parent._this->path,
                          [](std::string &s, const std::string &piece) -> std::string { return s += "/" + piece; })) {

}
Path::Path(Path const &parent, std::vector<std::string> const &relative) :
    Pimpl(parent._this->share, parent._this->node,
          std::accumulate(relative.begin(), relative.end(), parent._this->path,
                          [](std::string &s, const std::string &piece) -> std::string { return s += "/" + piece; })) {

}

Path Path::parent() const {
  unsigned long i = _this->path.rfind('/');
  if (i == 0) {
    return Path(_this->share, _this->node, "/");
  }
  if (i != std::string::npos) {
    std::string parentpath = _this->path.substr(0, i);
    return Path(_this->share, _this->node, parentpath);
  }

  return *this;
}

Path::Path(std::string const &share, std::string const &node, std::string const &path) :
  Pimpl(share, node, path) {

}
std::string Path::toString() const {
  if (_this->share.empty()) return _this->path;
  return _this->share + ":" + _this->node + ":" + _this->path;
}


}